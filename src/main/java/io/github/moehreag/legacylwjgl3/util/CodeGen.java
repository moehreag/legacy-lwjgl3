package io.github.moehreag.legacylwjgl3.util;

import java.util.ArrayList;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class CodeGen {
    /**
     * Creates the proper LVT load instruction given a type.
     *
     * @param type  The type of the local variable.
     * @param index The index into the LVT of the variables.
     * @return A load instruction.
     */
    public static AbstractInsnNode load(Type type, int index) {
        return new VarInsnNode(switch (type.getSort()) {
            case Type.BOOLEAN, Type.CHAR, Type.BYTE, Type.SHORT, Type.INT -> Opcodes.ILOAD;
            case Type.FLOAT -> Opcodes.FLOAD;
            case Type.LONG -> Opcodes.LLOAD;
            case Type.DOUBLE -> Opcodes.DLOAD;
            case Type.ARRAY, Type.OBJECT, Type.METHOD -> Opcodes.ALOAD;
            default -> throw new IllegalArgumentException("Unexpected type: " + type.getSort());
        }, index);
    }

    /**
     * Creates the proper invoke* instruction given a method.
     *
     * @param owner  The owner of the method.
     * @param target The target method.
     * @return The invoke instruction.
     */
    public static AbstractInsnNode invoke(ClassNode owner, MethodNode target) {
        final var opc = ((target.access & Opcodes.ACC_STATIC) == 0) ? Opcodes.INVOKEVIRTUAL : Opcodes.INVOKESTATIC;
        return new MethodInsnNode(
            opc, owner.name, target.name, target.desc,
            (owner.access & Opcodes.ACC_INTERFACE) != 0
        );
    }

    /**
     * Creates the proper return method for a given return type.
     *
     * @param returnType The return type.
     * @return A return instruction.
     */
    public static AbstractInsnNode ret(Type returnType) {
        return new InsnNode(switch (returnType.getSort()) {
            case Type.VOID -> Opcodes.RETURN;
            case Type.BOOLEAN, Type.CHAR, Type.BYTE, Type.SHORT, Type.INT -> Opcodes.IRETURN;
            case Type.FLOAT -> Opcodes.FRETURN;
            case Type.LONG -> Opcodes.LRETURN;
            case Type.DOUBLE -> Opcodes.DRETURN;
            case Type.ARRAY, Type.OBJECT, Type.METHOD -> Opcodes.ARETURN;
            default -> throw new IllegalArgumentException("Unsupported type: " + returnType.getSort());
        });
    }

    /**
     * Creates a method that forwards calls to another method.
     *
     * @param owner         The owner of both the target, and where the stub method needs to be defined in. This
     *                      method does not add the newly created method to the class.
     * @param target        The invocation target.
     * @param newMethodName The name of the new method.
     * @return The method node, which needs to be added to {@code owner}.
     */
    public static MethodNode createDelegatingMethod(ClassNode owner, MethodNode target, String newMethodName) {
        final var newNode = new MethodNode();
        // copy over the important stuff
        newNode.access = target.access;
        newNode.name = newMethodName;
        newNode.desc = target.desc;
        newNode.signature = target.signature;
        newNode.exceptions = new ArrayList<>(target.exceptions);

        int argIdx = 0;

        if ((target.access & Opcodes.ACC_STATIC) == 0) {
            newNode.instructions.add(new VarInsnNode(Opcodes.ALOAD, argIdx++));
        }

        // load args -> stack
        for (final var argumentType : Type.getArgumentTypes(newNode.desc)) {
            newNode.instructions.add(CodeGen.load(argumentType, argIdx));
            argIdx += argumentType.getSize();
        }

        // invoke the method
        newNode.instructions.add(CodeGen.invoke(owner, target));
        newNode.instructions.add(CodeGen.ret(Type.getReturnType(target.desc)));
        return newNode;
    }
}

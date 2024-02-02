package io.github.moehreag.legacylwjgl3.util;

import lombok.Data;

@Data
public class Triple<A, B, C> {
	private final A left; private final B middle; private final C right;

	public static <A, B, C> Triple<A, B, C> of(A a, B b, C c){
		return new Triple<>(a, b, c);
	}
}

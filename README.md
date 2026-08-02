# legacy-lwjgl3

A mod for [Ornithe](https://ornithemc.net) to run old versions of Minecraft with [lwjgl3](https://github.com/lwjgl/lwjgl3).

### Usage

#### Configuration

The mod offers optional configuration options via system properties or
environment variables.

A list of currently available options is provided below.

| Property Name                     | Environment variable name         | Default | Description                                           |
|-----------------------------------|-----------------------------------|---------|-------------------------------------------------------|
| `legacy_lwjgl3.use_sdl`           | `LEGACY_LWJGL3_USE_SDL`           | `false` | Use SDL3 instead of GLFW for window & input handling  |
| `legacy_lwjgl3.scale_framebuffer` | `LEGACY_LWJGL3_SCALE_FRAMEBUFFER` | `true`  | Enable framebuffer scaling on HiDPI displays          |

### Unstable versions (CI)

Bleeding-Edge versions can be found in the Actions tab of the repository: https://github.com/moehreag/legacy-lwjgl3/actions.

### Building

This mod can be built using `./gradlew build`, jars can then be found at `build/libs/`.

### Dev

This mod is published to AxolotlClient's maven, located at https://maven.axolotlclient.com.


```kotlin
repositories {
    maven("https://maven.axolotlclient.com/releases")
    //maven("https://maven.axolotlclient.com/snapshots") // for unstable versions, optional
}

dependencies {
	modImplementation("io.github.moehreag:legacy-lwjgl3:<VERSION>")
}
```

### IME support for other mods

This mod provides IME preedit overlay functionality. If other mods provide independent text field implementations
they will not integrate with IME by default. legacy-lwjgl3 publishes a small API package which allows other mods
to integrate with IME input.

```kotlin
repositories {
    maven("https://maven.axolotlclient.com/releases")
    //maven("https://maven.axolotlclient.com/snapshots") // for unstable versions, optional
}

dependencies {
	modImplementation("io.github.moehreag.legacy-lwjgl3:api:<VERSION>")
}
```

### Contributing

Contributions are welcome! Due to the project structure and its goal to support as many Minecraft versions
as possible working with the codebase is not trivial. If you are interested in contributing and need
assistance please join our [Discord server](https://discord.gg/BfmYmPw3Ts)

### Credits

This mod is forked from the version for legacyfabric authored by Zarzelcow: https://github.com/Zarzelcow/legacy-lwjgl3.
It is based on [lwjgl2](https://github.com/lwjgl/lwjgl) and a forge mod by gudenau: https://github.com/gudenau/MC-LWJGL3.

Additional Credits to:
 - The OrnitheMC Project: https://ornithemc.net
 - Contributors to this mod, especially: [Floweynt](https://github.com/Floweynt)

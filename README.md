# mitchell-n-ness

## setup

```sh
curl -s https://get.sdkman.io | bash
```

see also:

- https://sdkman.io/install
- https://github.com/sdkman/sdkman-cli

```sh
sdk install java 17.0.4.1-tem
```

## run

### as maven exec task

- `./mvnw exec:exec` _macOS | \*nix_
- `mvnw exec:exec` _Windows_

### as fat jar

- package
  - `./mvnw clean package` _macOS | \*nix_
  - `mvnw clean package` _Windows_

#### windows | \*nix

```sh
java -jar ./target/mitchell-n-ness-0.1.0-SNAPSHOT-jar-with-dependencies.jar
```

#### macos

```sh
java -XstartOnFirstThread -jar ./target/mitchell-n-ness-0.1.0-SNAPSHOT-jar-with-dependencies.jar
```

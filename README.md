# maven-plugin-install

This Maven plugin installs resolved Nabu `.nar` dependencies into the repository layout expected by Nabu projects.

If your project is located at:

```text
/home/user/repository/project
```

and it depends on:

```xml
<dependency>
	<groupId>nabu.types</groupId>
	<artifactId>structure</artifactId>
	<type>nar</type>
</dependency>
```

then the plugin installs that dependency to:

```text
/home/user/repository/nabu/types/structure.nar
```

## 1. Configure access to GitHub Packages

The `.nar` files are resolved through Maven, so your Maven setup must be able to read from GitHub Packages.

Add a server entry with a GitHub token that has package read access.

Example `~/.m2/settings.xml`:

```xml
<settings>
	<servers>
		<server>
			<id>github</id>
			<username>YOUR_GITHUB_USERNAME</username>
			<password>YOUR_GITHUB_TOKEN</password>
		</server>
	</servers>
</settings>
```

The token should be allowed to read packages from the GitHub organization or repository that hosts the Nabu packages.

Then add the GitHub Packages repository to your project `pom.xml`:

```xml
<repositories>
	<repository>
		<id>github</id>
		<name>GitHub Packages</name>
		<url>https://maven.pkg.github.com/nabu-platform/maven</url>
	</repository>
</repositories>
```

The `<id>` in the repository must match the `<server>` id in `settings.xml`.

## 2. Import the centralized BOM

Version management should normally be done through the centralized Nabu BOM, not by setting versions on each dependency.

Add the BOM in `dependencyManagement`:

```xml
<dependencyManagement>
	<dependencies>
		<dependency>
			<groupId>be.nabu</groupId>
			<artifactId>modules-bom</artifactId>
			<version>YOUR_BOM_VERSION</version>
			<type>pom</type>
			<scope>import</scope>
		</dependency>
	</dependencies>
</dependencyManagement>
```

This is the default and recommended setup.

## 3. Add Nabu dependencies

Add your Nabu dependencies without specifying a version when that version is managed by the BOM.

Example:

```xml
<dependencies>
	<dependency>
		<groupId>nabu.types</groupId>
		<artifactId>structure</artifactId>
		<type>nar</type>
	</dependency>
</dependencies>
```

Important: this plugin currently installs only dependencies resolved by Maven as type `nar`, so the dependency should explicitly declare:

```xml
<type>nar</type>
```

If you need to override a version in a special case, you can still set it explicitly on the dependency:

```xml
<dependency>
	<groupId>nabu.types</groupId>
	<artifactId>structure</artifactId>
	<version>1.13-SNAPSHOT.20260616130811</version>
	<type>nar</type>
</dependency>
```

## 4. Add the plugin to your project

Add the plugin to the `<build>` section of your `pom.xml`:

```xml
<build>
	<plugins>
		<plugin>
			<groupId>nabu</groupId>
			<artifactId>maven-plugin-install</artifactId>
			<version>1.0-SNAPSHOT</version>
			<executions>
				<execution>
					<goals>
						<goal>install-dependencies</goal>
					</goals>
				</execution>
			</executions>
		</plugin>
	</plugins>
</build>
```

By default, the plugin installs dependencies relative to the parent of the project directory.

So if the project lives in:

```text
/home/user/repository/project
```

then dependencies are installed under:

```text
/home/user/repository
```

## 5. Run the plugin

You can run it directly:

```bash
mvn nabu:maven-plugin-install:1.0-SNAPSHOT:install-dependencies
```

Or, if you configured it in the build, run the normal lifecycle phase that includes `process-resources`:

```bash
mvn process-resources
```

or:

```bash
mvn package
```

## 6. Optional custom target directory

If you want to install the `.nar` files somewhere else, override the target repository directory:

```bash
mvn -Dinstall.repositoryDirectory=/home/user/repository nabu:maven-plugin-install:1.0-SNAPSHOT:install-dependencies
```

## Summary

The setup consists of:

1. configure GitHub Packages credentials in `settings.xml`
2. add the GitHub Packages repository to the project POM
3. import the centralized Nabu BOM
4. declare Nabu dependencies with `<type>nar</type>`
5. add `nabu:maven-plugin-install`
6. run `mvn process-resources` or invoke the goal directly

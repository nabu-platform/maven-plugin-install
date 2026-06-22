/*
* Copyright (C) 2026 Alexander Verbruggen
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program. If not, see <https://www.gnu.org/licenses/>.
*/

package be.nabu.maven.install;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

@Mojo(name = "install-dependencies", defaultPhase = LifecyclePhase.PROCESS_RESOURCES, threadSafe = true, requiresDependencyResolution = org.apache.maven.plugins.annotations.ResolutionScope.TEST)
public class DependencyInstallMojo extends AbstractMojo {
	@Parameter(defaultValue = "${project}", required = true, readonly = true)
	private MavenProject project;

	@Parameter(defaultValue = "${project.basedir}", required = true, readonly = true)
	private File projectDirectory;

	@Parameter(property = "install.repositoryDirectory")
	private File repositoryDirectory;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		File targetRepositoryDirectory = repositoryDirectory == null ? projectDirectory.getParentFile() : repositoryDirectory;
		if (targetRepositoryDirectory == null) {
			throw new MojoExecutionException("Could not determine repository directory for project: " + projectDirectory);
		}
		Set<Artifact> artifacts = project.getArtifacts();
		if (artifacts == null || artifacts.isEmpty()) {
			getLog().info("No resolved dependencies found");
			return;
		}
		for (Artifact artifact : artifacts) {
			if (!"nar".equals(artifact.getType())) {
				continue;
			}
			File sourceFile = artifact.getFile();
			if (sourceFile == null || !sourceFile.isFile()) {
				throw new MojoExecutionException("Resolved nar dependency has no file: " + artifact);
			}
			File targetFile = new File(
				targetRepositoryDirectory,
				artifact.getGroupId().replace('.', File.separatorChar) + File.separator + artifact.getArtifactId()
					+ ".nar"
			);
			File parentFile = targetFile.getParentFile();
			if (!parentFile.exists() && !parentFile.mkdirs()) {
				throw new MojoExecutionException("Could not create target directory: " + parentFile);
			}
			copy(sourceFile, targetFile, artifact);
		}
	}

	private void copy(File sourceFile, File targetFile, Artifact artifact) throws MojoExecutionException {
		try {
			Files.copy(sourceFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			getLog().info("Installed nar dependency " + artifact + " to " + targetFile.getAbsolutePath());
		}
		catch (IOException e) {
			throw new MojoExecutionException("Could not install nar dependency " + artifact + " to " + targetFile, e);
		}
	}
}

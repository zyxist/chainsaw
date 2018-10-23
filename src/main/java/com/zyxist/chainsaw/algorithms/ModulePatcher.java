/*
 * Copyright 2017-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zyxist.chainsaw.algorithms;

import com.zyxist.chainsaw.jigsaw.cli.PatchItem;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Generates the '--patch-module' flag configuration from dependencies, and
 * patch settings set in 'javaModule' section.
 */
public class ModulePatcher {
	private static final Logger LOGGER = Logging.getLogger(ModulePatcher.class);
	private final Map<String, String> patches;

	public ModulePatcher(Map<String, String> patches) {
		this.patches = patches;
	}

	public Map<String, PatchItem> patchFrom(Project project, String ... dependencySets) {
		Map<String, PatchItem> patchList = new LinkedHashMap<>();
		for (String depSet: dependencySets) {
			Configuration cfg = project.getConfigurations().findByName(depSet);
			if (null != cfg && cfg.isCanBeResolved()) {
				scanDependencySet(patchList, cfg.getResolvedConfiguration().getResolvedArtifacts());
			}
		}
		return patchList;
	}

	private void scanDependencySet(Map<String, PatchItem> patchCommands, Set<ResolvedArtifact> resolvedArtifacts) {
		for (ResolvedArtifact artifact : resolvedArtifacts) {
			for (Map.Entry<String, String> patched : patches.entrySet()) {
				LOGGER.info("'"+patched.getKey()+"' vs '" + toGenericName(artifact)+"'");
				if (patched.getKey().equals(toGenericName(artifact))) {
					if (LOGGER.isEnabled(LogLevel.INFO)) {
						LOGGER.info("Module '" + patched.getValue() + "' will be patched with '" + toGenericName(artifact) + "'");
					}
					toList(patchCommands, patched.getValue(), artifact.getFile().getAbsolutePath());
				}
			}
		}
	}

	private void toList(Map<String, PatchItem> patchCommands, String patchedModule, String patchingJar) {
		PatchItem item = patchCommands.get(patchedModule);
		if (null == item) {
			item = new PatchItem(patchedModule);
			patchCommands.put(patchedModule, item);
		}
		item.with(patchingJar);
	}

	private String toGenericName(ResolvedArtifact artifact) {
		return artifact.getModuleVersion().getId().getGroup() + File.pathSeparator + artifact.getModuleVersion().getId().getName();
	}


}

/*******************************************************************************
 * Copyright 2018, 2021 Jorel Ali (Skepter) - MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *******************************************************************************/
package dev.jorel.commandapi;

import dev.jorel.commandapi.exceptions.UnsupportedVersionException;
import dev.jorel.commandapi.nms.NMS_1_16_R3;
import dev.jorel.commandapi.nms.NMS_1_17;
import dev.jorel.commandapi.nms.NMS_1_17_R1;
import dev.jorel.commandapi.nms.NMS_1_18_R1;
import dev.jorel.commandapi.nms.NMS_1_18_R2;
import dev.jorel.commandapi.nms.NMS_1_19_1_R1;
import dev.jorel.commandapi.nms.NMS_1_19_3_R2;
import dev.jorel.commandapi.nms.NMS_1_19_4_R3;
import dev.jorel.commandapi.nms.NMS_1_19_R1;
import dev.jorel.commandapi.nms.NMS_1_20_R1;
import dev.jorel.commandapi.nms.NMS_1_20_R2;
import dev.jorel.commandapi.nms.NMS_1_20_R3;
import dev.jorel.commandapi.nms.NMS_1_20_R4;
import dev.jorel.commandapi.nms.NMS_1_21_R1;
import dev.jorel.commandapi.nms.NMS_1_21_R2;
import dev.jorel.commandapi.nms.NMS_1_21_R3;
import org.bukkit.Bukkit;

/**
 * This file handles the NMS version to be loaded. The CommandAPIVersionHandler
 * file within the commandapi-core module is NOT used at compile time. Instead,
 * the commandapi-vh module is loaded instead, which doesn't use reflection to
 * load NMS instances.
 * 
 * NMS classes implement {@code NMS<CommandListenerWrapper>}. The type
 * CommandListenerWrapper isn't visible as in this Maven module (it's not
 * included and in some cases, cannot be included because Maven will only select
 * one version of a specific project (in this case, it'll only import one copy
 * of Spigot, almost always the latest stable version (from 1.16.5)). In
 * Eclipse, this can produce an error saying that a class cannot be resolved
 * because it is indirectly referenced from required .class files So sidestep
 * this, we introduce a second intermediary class NMSWrapper_VERSION which
 * depends on CommandListenerWrapper and then our main NMS_VERSION class extends
 * that.
 */
public abstract class CommandAPIVersionHandler {

	/**
	 * Returns an instance of the current running version's implementation of the Bukkit NMS.
	 *
	 * @return an instance of NMS which can run on the specified Minecraft version
	 */
	static LoadContext getPlatform() {
		String latestMajorVersion = "21"; // Change this for Minecraft's major update
		if (CommandAPI.getConfiguration().shouldUseLatestNMSVersion()) {
			return new LoadContext(new NMS_1_21_R3(), () -> {
				CommandAPI.logWarning("Loading the CommandAPI with the latest and potentially incompatible NMS implementation.");
				CommandAPI.logWarning("While you may find success with this, further updates might be necessary to fully support the version you are using.");
			});
		} else {
			String version = Bukkit.getBukkitVersion().split("-")[0];
			CommandAPIPlatform<?, ?, ?> platform = switch (version) {
				case "1.16.5" -> new NMS_1_16_R3();
				case "1.17" -> new NMS_1_17();
				case "1.17.1" -> new NMS_1_17_R1();
				case "1.18", "1.18.1" -> new NMS_1_18_R1();
				case "1.18.2" -> new NMS_1_18_R2();
				case "1.19" -> new NMS_1_19_R1();
				case "1.19.1", "1.19.2" -> new NMS_1_19_1_R1();
				case "1.19.3" -> new NMS_1_19_3_R2();
				case "1.19.4" -> new NMS_1_19_4_R3();
				case "1.20", "1.20.1" -> new NMS_1_20_R1();
				case "1.20.2" -> new NMS_1_20_R2();
				case "1.20.3", "1.20.4" -> new NMS_1_20_R3();
				case "1.20.5", "1.20.6" -> new NMS_1_20_R4();
				case "1.21", "1.21.1" -> new NMS_1_21_R1();
				case "1.21.2", "1.21.3" -> new NMS_1_21_R2();
				case "1.21.4" -> new NMS_1_21_R3();
				default -> null;
			};
			if (platform != null) {
				return new LoadContext(platform);
			}
			if (CommandAPI.getConfiguration().shouldBeLenientForMinorVersions()) {
				String currentMajorVersion = version.split("\\.")[1];
				if (latestMajorVersion.equals(currentMajorVersion)) {
					return new LoadContext(new NMS_1_21_R3(), () -> {
						CommandAPI.logWarning("Loading the CommandAPI with a potentially incompatible NMS implementation.");
						CommandAPI.logWarning("While you may find success with this, further updates might be necessary to fully support the version you are using.");
					});
				}
			}
			throw new UnsupportedVersionException(version);
		}
	}

}

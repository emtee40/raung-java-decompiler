package io.github.skylot.raung.asm.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.skylot.raung.asm.impl.parser.RaungAsmWriter;
import io.github.skylot.raung.asm.impl.parser.RaungParser;
import io.github.skylot.raung.asm.impl.parser.data.ClassData;
import io.github.skylot.raung.asm.impl.utils.RaungAsmException;
import io.github.skylot.raung.asm.impl.utils.ValidateAsmArgs;
import io.github.skylot.raung.common.utils.FileUtils;

public class RaungAsmExecutor {
	private static final Logger LOG = LoggerFactory.getLogger(RaungAsmExecutor.class);

	public static void process(RaungAsmBuilder args) {
		ValidateAsmArgs.process(args);
		String ext = FileUtils.getExt(args.getOutput());
		if (ext == null) {
			saveToDir(args);
		} else {
			switch (ext) {
				case "jar":
					saveToJar(args);
					break;
				case "class":
					saveToClass(args);
					break;
			}
		}
		LOG.info("Done");
	}

	public static byte[] processInputStream(RaungAsmBuilder args, InputStream input, @Nullable String fileName) {
		ValidateAsmArgs.process(args);
		return runForInputStream(args, input, fileName).getBytes();
	}

	public static byte[] processSingleClass(RaungAsmBuilder args, Path inputPath) {
		ValidateAsmArgs.process(args);
		return runForSingleClass(args, inputPath).getBytes();
	}

	public static ClassData runForSingleClass(RaungAsmBuilder args, Path inputPath) {
		try (InputStream input = Files.newInputStream(inputPath);
				RaungParser parser = new RaungParser(args, input, inputPath.toString())) {
			return RaungAsmWriter.buildCls(parser.parse());
		} catch (IOException e) {
			throw new RaungAsmException("Failed to assemble input", e);
		}
	}

	private static ClassData runForInputStream(RaungAsmBuilder args, InputStream input, @Nullable String fileName) {
		try (RaungParser parser = new RaungParser(args, input, fileName)) {
			return RaungAsmWriter.buildCls(parser.parse());
		} catch (IOException e) {
			throw new RaungAsmException("Failed to assemble input", e);
		}
	}

	private static void saveToDir(RaungAsmBuilder args) {
		throw new RaungAsmException("Not implemented yet");
	}

	private static void saveToClass(RaungAsmBuilder args) {
		Path input = args.getInputs().get(0);
		try {
			byte[] bytes = runForSingleClass(args, input).getBytes();
			Files.write(args.getOutput(), bytes, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
		} catch (Exception e) {
			throw new RaungAsmException("Failed to save class file from " + input.toAbsolutePath(), e);
		}
	}

	private static void saveToJar(RaungAsmBuilder args) {
		Path jarPath = args.getOutput();
		LOG.info("Saving to jar: {}", jarPath);
		try (JarOutputStream jar = new JarOutputStream(Files.newOutputStream(args.getOutput()))) {
			for (Path file : FileUtils.expandDirs(args.getInputs())) {
				String ext = FileUtils.getExt(file);
				if (Objects.equals(ext, "raung")) {
					ClassData cls = runForSingleClass(args, file);
					JarEntry entry = new JarEntry(cls.getName() + ".class");
					jar.putNextEntry(entry);
					jar.write(cls.getBytes());
					jar.closeEntry();
				}
			}
		} catch (IOException e) {
			throw new RaungAsmException("Process failed", e);
		}
	}
}
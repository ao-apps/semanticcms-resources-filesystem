/*
 * semanticcms-resources-filesystem - Redistributable sets of SemanticCMS resources stored in the filesystem.
 * Copyright (C) 2017, 2018, 2019, 2020, 2021  AO Industries, Inc.
 *     support@aoindustries.com
 *     7262 Bull Pen Cir
 *     Mobile, AL 36695
 *
 * This file is part of semanticcms-resources-filesystem.
 *
 * semanticcms-resources-filesystem is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * semanticcms-resources-filesystem is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with semanticcms-resources-filesystem.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.semanticcms.resources.filesystem;

import com.aoapps.net.Path;
import com.semanticcms.core.resources.ResourceStore;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.nio.file.NotDirectoryException;
import java.util.HashMap;
import java.util.Map;

/**
 * Accesses resources in the local filesystem.
 */
public class FilesystemResourceStore implements ResourceStore {

	private static final Map<String, FilesystemResourceStore> filesystemStores = new HashMap<>();

	/**
	 * Gets the filesystem store for the given directory.
	 * Only one {@link FilesystemResourceStore} is created per unique {@link File#getCanonicalPath()}.
	 *
	 * @throws  FileNotFoundException if file not found
	 * @throws  NotDirectoryException  if not directory
	 * @throws  IOException  not readable
	 */
	public static FilesystemResourceStore getInstance(File directory) throws FileNotFoundException, NotDirectoryException, IOException {
		if(!directory.exists()) throw new FileNotFoundException(directory.toString());
		if(!directory.isDirectory()) throw new NotDirectoryException("Not a directory: " + directory.toString());
		if(!directory.canRead()) throw new IOException("Unable to read directory: " + directory.toString());
		File canonicalDirectory = directory.getCanonicalFile();
		String canonicalPath = canonicalDirectory.getCanonicalPath();
		// This double checking on canonical directory might be a bit obsessive, but stores are infrequently created so why not?
		if(!canonicalDirectory.exists()) throw new FileNotFoundException(canonicalPath);
		if(!canonicalDirectory.isDirectory()) throw new NotDirectoryException("Not a directory: " + canonicalPath);
		if(!canonicalDirectory.canRead()) throw new IOException("Unable to read directory: " + canonicalPath);
		synchronized(filesystemStores) {
			FilesystemResourceStore filesystemStore = filesystemStores.get(canonicalPath);
			if(filesystemStore == null) {
				filesystemStore = new FilesystemResourceStore(canonicalDirectory);
				filesystemStores.put(canonicalPath, filesystemStore);
			}
			return filesystemStore;
		}
	}

	private final File directory;

	private FilesystemResourceStore(File directory) {
		this.directory = directory;
	}

	public File getDirectory() {
		return directory;
	}

	@Override
	public String toString() {
		try {
			return directory.toURI().toURL().toExternalForm();
		} catch(MalformedURLException e) {
			throw new UncheckedIOException(e);
		}
	}

	@Override
	public boolean isAvailable() {
		// TODO: ?: return directory.exists();  // Allow unavailable on start-up, too?
		return true;
	}

	/**
	 * @param path  Must be a {@link FilesystemResource#checkFilesystemPath(com.aoapps.net.Path) valid filesystem path}
	 */
	@Override
	public FilesystemResource getResource(Path path) {
		FilesystemResource.checkFilesystemPath(path);
		String pathStr = path.toString();
		File file;
		if(pathStr.equals("/")) {
			file = directory;
		} else {
			String subpath;
			if(pathStr.endsWith("/")) {
				// Skip first slash and strip ending slash
				subpath = pathStr.substring(1, pathStr.length() - 1);
			} else {
				// Skip first slash
				subpath = pathStr.substring(1);
			}
			file = new File(directory, subpath.replace('/', File.separatorChar));
		}
		return new FilesystemResource(this, path, file);
	}
}

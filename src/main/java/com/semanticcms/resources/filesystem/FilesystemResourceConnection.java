/*
 * semanticcms-resources-filesystem - Redistributable sets of SemanticCMS resources stored in the filesystem.
 * Copyright (C) 2017  AO Industries, Inc.
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

import com.semanticcms.core.resources.ResourceConnection;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class FilesystemResourceConnection extends ResourceConnection {

	final File file;
	private FileInputStream in;
	private List<FilesystemResourceFile> resourceFiles;
	private boolean closed;

	public FilesystemResourceConnection(FilesystemResource resource, File file) {
		super(resource);
		this.file = file;
	}

	@Override
	public FilesystemResource getResource() {
		return (FilesystemResource)resource;
	}

	@Override
	public boolean exists() throws IOException, IllegalStateException {
		if(closed) throw new IllegalStateException("Connection closed: " + resource);
		return file.exists();
	}

	@Override
	public long getLength() throws IOException, FileNotFoundException, IllegalStateException {
		if(closed) throw new IllegalStateException("Connection closed: " + resource);
		if(!file.exists()) throw new FileNotFoundException(file.getPath());
		// TODO: Java 1.7: Handle 0 as unknown to convert to -1: Files.readAttributes
		//                 Could do some reflection tricks to avoid hard dependency on Java 1.7, or just bump our java version globally.
		return file.length();
	}

	@Override
	public long getLastModified() throws IOException, FileNotFoundException, IllegalStateException {
		if(closed) throw new IllegalStateException("Connection closed: " + resource);
		if(!file.exists()) throw new FileNotFoundException(file.getPath());
		return file.lastModified();
	}

	@Override
	public InputStream getInputStream() throws IOException, FileNotFoundException, IllegalStateException {
		if(closed) throw new IllegalStateException("Connection closed: " + resource);
		if(in != null) throw new IllegalStateException("Input already opened: " + resource.toString());
		in = new FileInputStream(file);
		return in;
	}

	@Override
	public FilesystemResourceFile getResourceFile() throws IOException, FileNotFoundException, IllegalStateException {
		if(closed) throw new IllegalStateException("Connection closed: " + resource);
		FilesystemResourceFile resourceFile = new FilesystemResourceFile(this);
		if(resourceFiles == null) resourceFiles = new ArrayList<FilesystemResourceFile>();
		resourceFiles.add(resourceFile);
		return resourceFile;
	}

	@Override
	public void close() throws IOException {
		if(in != null) in.close();
		if(resourceFiles != null && !resourceFiles.isEmpty()) {
			FilesystemResourceFile[] closeMe = resourceFiles.toArray(new FilesystemResourceFile[resourceFiles.size()]);
			for(int i = closeMe.length - 1; i >= 0; i--) {
				closeMe[i].close();
			}
			assert resourceFiles.isEmpty();
		}
		closed = true;
	}

	/**
	 * @see  FilesystemResourceFile#close()
	 */
	void onResourceFileClosed(FilesystemResourceFile closed) {
		assert resourceFiles != null;
		for(int i = resourceFiles.size() - 1; i >= 0; i--) {
			if(resourceFiles.get(i) == closed) {
				resourceFiles.remove(i);
				break;
			}
		}
	}
}

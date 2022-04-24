/*
 * semanticcms-resources-filesystem - Redistributable sets of SemanticCMS resources stored in the filesystem.
 * Copyright (C) 2017, 2018, 2019, 2022  AO Industries, Inc.
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
 * along with semanticcms-resources-filesystem.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.semanticcms.resources.filesystem;

import com.semanticcms.core.resources.ResourceConnection;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class FilesystemResourceConnection extends ResourceConnection {

  private final File file;
  private FileInputStream in;
  private boolean fileAccessed;
  private boolean closed;

  public FilesystemResourceConnection(FilesystemResource resource, File file) {
    super(resource);
    this.file = file;
  }

  @Override
  public FilesystemResource getResource() {
    return (FilesystemResource) resource;
  }

  @Override
  public boolean exists() throws IOException, IllegalStateException {
    if (closed) {
      throw new IllegalStateException("Connection closed: " + resource);
    }
    return file.exists();
  }

  @Override
  public long getLength() throws IOException, FileNotFoundException, IllegalStateException {
    if (closed) {
      throw new IllegalStateException("Connection closed: " + resource);
    }
    if (!file.exists()) {
      throw new FileNotFoundException(file.getPath());
    }
    // TODO: Handle 0 as unknown to convert to -1: Files.readAttributes
    return file.length();
  }

  @Override
  public long getLastModified() throws IOException, FileNotFoundException, IllegalStateException {
    if (closed) {
      throw new IllegalStateException("Connection closed: " + resource);
    }
    if (!file.exists()) {
      throw new FileNotFoundException(file.getPath());
    }
    return file.lastModified();
  }

  @Override
  public InputStream getInputStream() throws IOException, FileNotFoundException, IllegalStateException {
    if (closed) {
      throw new IllegalStateException("Connection closed: " + resource);
    }
    if (in != null) {
      throw new IllegalStateException("Input already opened: " + resource.toString());
    }
    if (fileAccessed) {
      throw new IllegalStateException("File already accessed: " + resource.toString());
    }
    in = new FileInputStream(file);
    return in;
  }

  @Override
  public File getFile() throws IOException, FileNotFoundException, IllegalStateException {
    if (closed) {
      throw new IllegalStateException("Connection closed: " + resource);
    }
    if (in != null) {
      throw new IllegalStateException("Input already opened: " + resource.toString());
    }
    if (!file.exists()) {
      throw new FileNotFoundException(file.getPath());
    }
    fileAccessed = true;
    return file;
  }

  @Override
  public void close() throws IOException {
    if (in != null) {
      in.close();
    }
    closed = true;
  }
}

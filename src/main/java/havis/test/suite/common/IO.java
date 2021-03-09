package havis.test.suite.common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;

public class IO {
	private final ApplicationContext context;

	public IO(ApplicationContext context) {
		this.context = context;
	}

	/**
	 * Loads a resource from an URI. Location URI must be relative to classpath
	 * Resource
	 * 
	 * @param locationURI
	 * @return
	 * @throws IOException
	 */
	public String loadResource(String locationURI) throws IOException {
		InputStream stream = null;
		InputStreamReader in = null;
		BufferedReader inBuffer = null;

		try {
			Resource resource = context.getResource(locationURI);
			stream = resource.getInputStream();
			in = new InputStreamReader(stream, StandardCharsets.UTF_8);
			inBuffer = new BufferedReader(in);

			inBuffer.mark(1);
			char[] possibleBOM = new char[1];
			inBuffer.read(possibleBOM);
			if (possibleBOM[0] != '\ufeff') {
				inBuffer.reset();
			}

			StringBuilder stringBuilder = new StringBuilder();
			String line = null;

			while ((line = inBuffer.readLine()) != null) {
				stringBuilder.append(line);
			}

			return stringBuilder.toString();

		} finally {
			if (stream != null) {
				stream.close();
			}

			if (in != null) {
				in.close();
			}

			if (inBuffer != null) {
				inBuffer.close();
			}

		}
	}

	/**
	 * Writes a resource to an URI. Only file URIs are supported up to now.
	 * 
	 * @param locationURI
	 * @param content
	 * @throws IOException
	 */
	public void writeResource(String locationURI, String content)
			throws IOException {
		if (content != null && content.length() != 0) {
			Writer out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(locationURI), "UTF-8"));
			out.write(normalizeToFileString(content));
			out.close();
		}
	}

	/**
	 * Delete files using a filter (name, count, size, time)
	 * 
	 * @param dir
	 *            full path to the directory with the files
	 * @param filterExpr
	 *            RegEx for file expression (e.g. "*.txt")
	 * @param maxCount
	 *            max. count of all remaining files
	 * @param maxSize
	 *            max. size of all remaining files (in kByte)
	 * @param time
	 *            end time for (see maxTimeInterval) (UTC)
	 * @param maxTimeInterval
	 *            time interval of all remaining files ending at (in seconds)
	 * @return count of deleted files
	 * @throws IOException
	 */
	public int deleteFiles(String dir, String filterExpr, Long maxCount,
			Long maxSize, Date time, Long maxTimeInterval) throws IOException {

		Map<FileTime, FileInfo> files;
		List<Path> directories;

		// sort files by last writing time in reverse order
		files = new TreeMap<FileTime, FileInfo>(Collections.reverseOrder());
		directories = new ArrayList<Path>();

		FileVisitor<Path> fileWalker = new FileWalker(filterExpr, files,
				directories);
		Files.walkFileTree(Paths.get(dir), fileWalker);

		long scannedFilesSize = 0;
		int remainingFilesCount = 0;
		int deletedFilesCount = 0;
		boolean delete = false;

		// for each file
		for (Entry<FileTime, FileInfo> file : files.entrySet()) {
			if (!delete) {
				// if max file count has reached
				if (maxCount != null && remainingFilesCount == maxCount) {
					delete = true;
					// else if max time interval has reached
				} else if (time != null
						&& maxTimeInterval != null
						&& (time.getTime() - file.getKey().toMillis()) > (maxTimeInterval * 1000)) {
					delete = true;
				} else {
					scannedFilesSize += file.getValue().getAttrs().size();
					// if max size has been reached
					if (maxSize != null && scannedFilesSize > maxSize * 1024) {
						delete = true;
					} else {
						// increase counter of remaining files
						remainingFilesCount++;
					}
				}
			}
			if (delete) {
				// delete the file
				Files.delete(file.getValue().getPath());
				deletedFilesCount++;
			}
		}

		// delete all directories
		Collections.reverse(directories);
		for (Path directory : directories) {
			File file = directory.toFile();
			if (file.isDirectory()) {
				if (file.list().length == 0) {
					Files.delete(file.toPath());
				}
			}
		}
		return deletedFilesCount;
	}

	/**
	 * Normalize String for output
	 * @param in
	 * @return
	 */
	public static String normalizeToFileString(String in) {
		in = in.replaceAll("(\r\n|\n|\r)", System.getProperty("line.separator"));
		return in;
	}

	/**
	 * This class is a data structure to stores necessary informations about
	 * files
	 */
	private class FileInfo {
		private BasicFileAttributes attrs;
		private Path path;

		public BasicFileAttributes getAttrs() {
			return attrs;
		}

		public void setAttrs(BasicFileAttributes attrs) {
			this.attrs = attrs;
		}

		public Path getPath() {
			return path;
		}

		public void setPath(Path file) {
			this.path = file;
		}
	}

	/**
	 * An implementation of a file visitor class to go through a file Tree
	 */
	private class FileWalker extends SimpleFileVisitor<Path> {

		private PathMatcher matcher;
		private Map<FileTime, FileInfo> files;
		private List<Path> directories;

		public FileWalker(String filterExpr, Map<FileTime, FileInfo> files,
				List<Path> directories) {
			matcher = FileSystems.getDefault().getPathMatcher(
					"glob:" + filterExpr);
			this.files = files;
			this.directories = directories;
		}

		/**
		 * See
		 * {@link java.nio.file.FileVisitor#visitFile(Object, BasicFileAttributes)}
		 */
		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
				throws IOException {
			Path name = file.getFileName();
			// If matcher matches, store file information and path with key time
			if (matcher != null && name != null && matcher.matches(name)) {
				FileInfo fileinfo = new FileInfo();
				fileinfo.setPath(file);
				fileinfo.setAttrs(attrs);
				files.put(attrs.lastModifiedTime(), fileinfo);
			}
			return FileVisitResult.CONTINUE;
		}

		/**
		 * See
		 * {@link java.nio.file.FileVisitor#preVisitDirectory(Object, BasicFileAttributes)}
		 */
		@Override
		public FileVisitResult preVisitDirectory(Path directory,
				BasicFileAttributes attrs) throws IOException {
			// Stores directories
			directories.add(directory);
			return FileVisitResult.CONTINUE;
		}

	}

}

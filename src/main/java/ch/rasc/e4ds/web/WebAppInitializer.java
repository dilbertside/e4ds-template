package ch.rasc.e4ds.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.DispatcherType;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;

import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.DispatcherServlet;

import ch.rasc.e4ds.config.ComponentConfig;
import ch.rasc.e4ds.config.DataConfig;
import ch.rasc.e4ds.config.ScheduleConfig;
import ch.rasc.e4ds.config.SecurityConfig;
import ch.rasc.e4ds.config.WebConfig;
import ch.rasc.e4ds.util.Base62;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.io.CharStreams;
import com.google.common.io.InputSupplier;
import com.yahoo.platform.yui.compressor.CssCompressor;
import com.yahoo.platform.yui.compressor.JavaScriptCompressor;

public class WebAppInitializer implements WebApplicationInitializer {

	@SuppressWarnings("resource")
	@Override
	public void onStartup(ServletContext container) {
		AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
		rootContext.setServletContext(container);
		rootContext.getEnvironment().setDefaultProfiles("production");
		rootContext.register(ComponentConfig.class, DataConfig.class, ScheduleConfig.class, SecurityConfig.class,
				WebConfig.class);

		container.addListener(new ContextLoaderListener(rootContext));
		container.addListener(HttpSessionEventPublisher.class);

		CharacterEncodingFilter encFilter = new CharacterEncodingFilter();
		encFilter.setEncoding("UTF-8");
		encFilter.setForceEncoding(true);
		container.addFilter("characterEncodingFilter", encFilter).addMappingForServletNames(
				EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.INCLUDE), true, "/*");

		container.addFilter("springSecurityFilterChain",
				new DelegatingFilterProxy("springSecurityFilterChain", rootContext)).addMappingForUrlPatterns(
				EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.ASYNC), true, "/*");

		container.addFilter("mdcFilter", MdcFilter.class).addMappingForUrlPatterns(
				EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD), true, "/*");

		DispatcherServlet servlet = new DispatcherServlet(new GenericWebApplicationContext());
		servlet.setThreadContextInheritable(true);

		ServletRegistration.Dynamic dispatcher = container.addServlet("dispatcher", servlet);
		dispatcher.setLoadOnStartup(1);
		dispatcher.addMapping("/");
		dispatcher.setAsyncSupported(true);

		try {
			processWebResources(container, rootContext.getEnvironment().acceptsProfiles("production"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	private final static Pattern DEV_CODE_PATTERN = Pattern.compile("/\\* <debug> \\*/.*?/\\* </debug> \\*/",
			Pattern.DOTALL);

	private void processWebResources(ServletContext container, boolean production) throws IOException {

		Properties versionProperties = readVersionProperties();

		Map<String, StringBuilder> htmlCodes = Maps.newHashMap();
		Map<String, StringBuilder> codes = Maps.newHashMap();

		try (InputStream is = getClass().getResourceAsStream("/webresources.txt")) {
			List<String> lines = CharStreams.readLines(CharStreams.newReaderSupplier(createInputSupplier(is),
					Charsets.UTF_8));

			String varName = null;

			for (String line : lines) {
				String l = line.trim();
				if (l.isEmpty() || l.startsWith("#")) {
					continue;
				}

				if (l.endsWith(":")) {
					varName = l.substring(0, l.length() - 1);
					htmlCodes.put(varName, new StringBuilder());
					codes.put(varName, new StringBuilder());
					continue;
				}

				if (varName == null) {
					continue;
				}

				int pos = l.indexOf("[");
				String mode = "p";
				if (pos != -1) {
					mode = l.substring(pos + 1, l.length() - 1);
					l = l.substring(0, pos);
				}

				l = replaceVariables(l, versionProperties);

				if (!production && mode.contains("d")) {
					htmlCodes.get(varName).append(createHtmlCode(container, l, varName));
				} else if (production && mode.contains("p")) {
					if (mode.contains("s")) {
						htmlCodes.get(varName).append(createHtmlCode(container, l, varName));
					} else {
						System.out.println("READ: " + l);
						try (InputStream lis = container.getResourceAsStream(l)) {
							String sourcecode = CharStreams.toString(CharStreams.newReaderSupplier(
									createInputSupplier(lis), Charsets.UTF_8));

							if (varName.endsWith("_js")) {
								Matcher matcher = DEV_CODE_PATTERN.matcher(sourcecode);
								StringBuffer cleanCode = new StringBuffer();
								while (matcher.find()) {
									matcher.appendReplacement(cleanCode, "");
								}
								matcher.appendTail(cleanCode);
								String minifiedJs = minifyJs(cleanCode.toString());
								codes.get(varName).append(minifiedJs).append('\n');
							} else if (varName.endsWith("_css")) {
								String changedCss = changeImageUrls(sourcecode, l);
								changedCss = compressCss(changedCss);
								codes.get(varName).append(changedCss);
							}
						}
					}
				}
			}

			for (Map.Entry<String, StringBuilder> entry : codes.entrySet()) {
				String key = entry.getKey();
				if (entry.getValue().length() > 0) {
					byte[] content = entry.getValue().toString().getBytes(StandardCharsets.UTF_8);

					if (key.endsWith("_js")) {
						String root = key.substring(0, key.length() - 3);

						String crc = Base62.generateMD5asBase62String(content);
						String servletPath = "/" + root + crc + ".js";
						container.addServlet(root + crc + "js", new ResourceServlet(content, "application/javascript"))
								.addMapping(servletPath);

						StringBuilder js = new StringBuilder();
						js.append("<script src=\"");
						js.append(container.getContextPath());
						js.append(servletPath);
						js.append("\"></script>");
						js.append("\n");

						htmlCodes.get(key).append(js);

					} else if (key.endsWith("_css")) {
						String root = key.substring(0, key.length() - 4);
						String crc = Base62.generateMD5asBase62String(content);
						String servletPath = "/" + root + crc + ".css";
						container.addServlet(root + crc + "css", new ResourceServlet(content, "text/css")).addMapping(
								servletPath);

						StringBuilder css = new StringBuilder();
						css.append("<link rel=\"stylesheet\" href=\"");
						css.append(container.getContextPath());
						css.append(servletPath);
						css.append("\">");
						css.append("\n");

						htmlCodes.get(key).append(css);
					}
				}
			}

			for (Map.Entry<String, StringBuilder> entry : htmlCodes.entrySet()) {
				container.setAttribute(entry.getKey(), entry.getValue());
			}

		}
	}

	private static StringBuilder createHtmlCode(ServletContext container, String line, String varName) {
		StringBuilder sb = new StringBuilder(100);
		if (varName.endsWith("_js")) {
			sb.append("<script src=\"");
			sb.append(container.getContextPath());
			sb.append(line);
			sb.append("\"></script>");
			sb.append("\n");
		} else if (varName.endsWith("_css")) {
			sb.append("<link rel=\"stylesheet\" href=\"");
			sb.append(container.getContextPath());
			sb.append(line);
			sb.append("\">");
			sb.append("\n");
		}
		return sb;
	}

	private final static Pattern CSS_URL_PATTERN = Pattern.compile("(.*?url.*?\\('*)([^\\)']*)('*\\))",
			Pattern.CASE_INSENSITIVE);

	private static String changeImageUrls(String css, String cssPath) {
		Matcher matcher = CSS_URL_PATTERN.matcher(css);
		StringBuffer sb = new StringBuffer();

		Path basePath = Paths.get(cssPath.substring(1));

		while (matcher.find()) {
			String url = matcher.group(2);
			url = url.trim();
			if (url.equals("#default#VML")) {
				continue;
			}
			Path pa = basePath.resolveSibling(url).normalize();
			matcher.appendReplacement(sb, "$1" + pa.toString().replace("\\", "/") + "$3");
		}
		matcher.appendTail(sb);
		return sb.toString();
	}

	private Properties readVersionProperties() throws IOException {
		Properties properties = new Properties();
		try (InputStream is = getClass().getResourceAsStream("/version.properties")) {
			properties.load(is);
		}
		return properties;
	}

	private static String minifyJs(String js) throws EvaluatorException, IOException {

		ErrorReporter e = new ErrorReporter() {

			@Override
			public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
				if (line < 0) {
					System.err.println("\n[WARNING] " + message);
				} else {
					System.err.println("\n[WARNING] " + line + ':' + lineOffset + ':' + message);
				}
			}

			@Override
			public void error(String message, String sourceName, int line, String lineSource, int lineOffset) {
				if (line < 0) {
					System.err.println("\n[ERROR] " + message);
				} else {
					System.err.println("\n[ERROR] " + line + ':' + lineOffset + ':' + message);
				}
			}

			@Override
			public EvaluatorException runtimeError(String message, String sourceName, int line, String lineSource,
					int lineOffset) {
				error(message, sourceName, line, lineSource, lineOffset);
				return new EvaluatorException(message);
			}
		};

		JavaScriptCompressor jsc = new JavaScriptCompressor(new StringReader(js), e);

		int linebreak = 120;
		boolean munge = false;
		boolean verbose = false;
		boolean preserveAllSemiColons = true;
		boolean disableOptimizations = true;

		StringWriter sw = new StringWriter();
		jsc.compress(sw, linebreak, munge, verbose, preserveAllSemiColons, disableOptimizations);
		return sw.toString();

	}

	private static String compressCss(String css) throws EvaluatorException, IOException {
		CssCompressor cc = new CssCompressor(new StringReader(css));
		int linebreak = 120;
		StringWriter sw = new StringWriter();
		cc.compress(sw, linebreak);
		return sw.toString();
	}

	private static String replaceVariables(String string, Properties versionProperties) {
		String s = string;
		for (Entry<Object, Object> entry : versionProperties.entrySet()) {
			String var = "{" + entry.getKey() + "}";
			s = s.replace(var, (String) entry.getValue());
		}
		return s;
	}

	private static InputSupplier<InputStream> createInputSupplier(final InputStream is) {
		return new InputSupplier<InputStream>() {
			@Override
			public InputStream getInput() {
				return is;
			}
		};
	}

}
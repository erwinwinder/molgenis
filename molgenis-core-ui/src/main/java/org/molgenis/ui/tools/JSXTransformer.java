package org.molgenis.ui.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.commonjs.module.Require;
import org.mozilla.javascript.commonjs.module.RequireBuilder;
import org.mozilla.javascript.commonjs.module.provider.SoftCachingModuleScriptProvider;
import org.mozilla.javascript.commonjs.module.provider.UrlModuleSourceProvider;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;

/**
 * Based on https://gist.github.com/mingfang/3784a0a6e58c24dda687
 */
public class JSXTransformer
{
	private List<String> modulePaths;
	private String jsxTransformerJS;
	private Context ctx;
	private Scriptable exports;
	private Scriptable topLevelScope;
	private Function transform;

	public static void main(String args[]) throws URISyntaxException
	{
		JSXTransformer jsxTransformer = new JSXTransformer();
		jsxTransformer.setModulePaths(Arrays.asList("/js/react"));
		jsxTransformer.setJsxTransformerJS("JSXTransformer.js");
		jsxTransformer.init();
		jsxTransformer.compileFiles("src/main/resources/jsx", "target/generated-resources/js");
	}

	public void setModulePaths(List<String> modulePaths)
	{
		this.modulePaths = modulePaths;
	}

	public void setJsxTransformerJS(String jsxTransformerJS)
	{
		this.jsxTransformerJS = jsxTransformerJS;
	}

	public void init() throws URISyntaxException
	{
		ctx = Context.enter();
		try
		{
			RequireBuilder builder = new RequireBuilder();
			builder.setModuleScriptProvider(new SoftCachingModuleScriptProvider(new UrlModuleSourceProvider(
					buildModulePaths(), null)));

			topLevelScope = ctx.initStandardObjects();
			Require require = builder.createRequire(ctx, topLevelScope);

			exports = require.requireMain(ctx, jsxTransformerJS);
			transform = (Function) exports.get("transform", topLevelScope);
		}
		finally
		{
			Context.exit();
		}
	}

	private List<URI> buildModulePaths() throws URISyntaxException
	{
		List<URI> uris = new ArrayList<URI>(modulePaths.size());
		for (String path : modulePaths)
		{
			try
			{
				URI uri = getClass().getResource(path).toURI();
				if (!uri.toString().endsWith("/"))
				{
					// make sure URI always terminates with slash to
					// avoid loading from unintended locations
					uri = new URI(uri + "/");
				}
				uris.add(uri);
			}
			catch (URISyntaxException usx)
			{
				throw new RuntimeException(usx);
			}
		}
		return uris;
	}

	public void compileFiles(String jsxFolder, String outputFolder)
	{
		try
		{
			Files.walk(Paths.get(jsxFolder)).filter(path -> path.toFile().isFile()).forEach(path -> {
				String uri = path.getParent().toString();
				String subFolders = uri.substring(jsxFolder.length(), uri.length());
				File f = new File(outputFolder, subFolders);
				if (!f.exists()) f.mkdirs();
				compile(path.toString(), f.getAbsolutePath());
			});
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
	}

	public void compile(String jsxFile, String outputFolder)
	{
		String jsxFileName = StringUtils.getFilename(jsxFile);
		String jsFileName = jsxFileName.substring(0, jsxFileName.length() - 1);
		File jsFile = new File(outputFolder, jsFileName);
		String js = transformFile(jsxFile);

		try
		{
			if (!jsFile.exists()) jsFile.createNewFile();
			FileCopyUtils.copy(js, new OutputStreamWriter(new FileOutputStream(jsFile), "UTF-8"));
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
	}

	public String transformFile(String jsxFile)
	{
		try
		{
			InputStream in = new FileInputStream(new File(jsxFile));
			String source = FileCopyUtils.copyToString(new InputStreamReader(in, "UTF-8"));
			return transform(source);
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
	}

	public String transform(String jsx)
	{
		Context.enter();
		try
		{
			NativeObject result = (NativeObject) transform.call(ctx, topLevelScope, exports, new String[]
			{ jsx });
			return result.get("code").toString();
		}
		finally
		{
			Context.exit();
		}
	}
}
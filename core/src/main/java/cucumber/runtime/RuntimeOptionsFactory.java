package cucumber.runtime;

import cucumber.api.CucumberOptions;
import cucumber.runtime.formatter.PluginFactory;
import cucumber.runtime.io.MultiLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;

public class RuntimeOptionsFactory {
    private final List<Class> clazzList;
    private boolean featuresSpecified = false;
    private boolean glueSpecified = false;
    private boolean pluginSpecified = false;

    public RuntimeOptionsFactory(List<Class> clazzList) {
        this.clazzList = clazzList;
    }

    static String packagePath(Class clazz) {
        return packagePath(packageName(clazz.getName()));
    }

    static String packagePath(String packageName) {
        return packageName.replace('.', '/');
    }

    static String packageName(String className) {
        return className.substring(0, Math.max(0, className.lastIndexOf(".")));
    }

    public RuntimeOptions create() {
        List<String> args = this.buildArgsFromOptions();
        return new RuntimeOptions(args);
    }

    private List<String> buildArgsFromOptions() {
        List<String> args = new ArrayList<>();
        List<String> features = new ArrayList<>();

        for (Class cl : clazzList) {
            for (Class classWithOptions = cl; this.hasSuperClass(classWithOptions); classWithOptions = classWithOptions.getSuperclass()) {
                CucumberOptions options = this.getOptions(classWithOptions);
                if (options != null) {
                    this.addDryRun(options, args);
                    this.addMonochrome(options, args);
                    this.addTags(options, args);
                    this.addPlugins(options, args);
                    this.addStrict(options, args);
                    this.addName(options, args);
                    this.addSnippets(options, args);
                    this.addGlue(options, args);
                    this.addFeatures(options, features);
                }
            }
        }
        args.addAll(features);

        this.addDefaultFeaturePathIfNoFeaturePathIsSpecified(args, clazzList.get(0));
        this.addDefaultGlueIfNoGlueIsSpecified(args, clazzList.get(0));
        this.addNullFormatIfNoPluginIsSpecified(args);
        return args;
    }

    private void addName(CucumberOptions options, List<String> args) {
        String[] var3 = options.name();
        int var4 = var3.length;

        for (int var5 = 0; var5 < var4; ++var5) {
            String name = var3[var5];
            setIfSet(args, name, "--name");
        }

    }

    private void addSnippets(CucumberOptions options, List<String> args) {
        addIfSet(args, options.snippets().toString(), "--snippets");
    }

    private void addDryRun(CucumberOptions options, List<String> args) {
        if (options.dryRun() && !args.contains("--dry-run")) {
            args.add("--dry-run");
        }

    }

    private void addMonochrome(CucumberOptions options, List<String> args) {
        if (options.monochrome() || this.runningInEnvironmentWithoutAnsiSupport() && !args.contains("--monochrome")) {
            args.add("--monochrome");
        }

    }

    private void addTags(CucumberOptions options, List<String> args) {
        String[] var3 = options.tags();
        int var4 = var3.length;

        for (int var5 = 0; var5 < var4; ++var5) {
            String tags = var3[var5];
            args.add("--tags");
            args.add(tags);
        }

    }

    private void addPlugins(CucumberOptions options, List<String> args) {
        ArrayList<String> plugins = new ArrayList<>();
        plugins.addAll(Arrays.asList(options.plugin()));
        plugins.addAll(Arrays.asList(options.format()));

        for (Iterator var4 = plugins.iterator(); var4.hasNext(); this.pluginSpecified = true) {
            String plugin = (String) var4.next();
            addIfSet(args, plugin, "--plugin");
        }

    }

    private void addNullFormatIfNoPluginIsSpecified(List<String> args) {
        if (!this.pluginSpecified) {
            args.add("--plugin");
            args.add("null");
        }

    }

    private void addFeatures(CucumberOptions options, List<String> args) {
        if (options != null && options.features().length != 0) {
            Collections.addAll(args, options.features());
            this.featuresSpecified = true;
        }

    }

    private void addDefaultFeaturePathIfNoFeaturePathIsSpecified(List<String> args, Class clazz) {
        if (!this.featuresSpecified) {
            args.add("classpath:" + packagePath(clazz));
        }

    }

    private void addGlue(CucumberOptions options, List<String> args) {
        String[] var3 = options.glue();
        int var4 = var3.length;

        for (int var5 = 0; var5 < var4; ++var5) {
            String glue = var3[var5];
            if (!args.contains(glue)) {
                args.add("--glue");
                args.add(glue);
                this.glueSpecified = true;
            }
        }

    }

    private void addDefaultGlueIfNoGlueIsSpecified(List<String> args, Class clazz) {
        if (!this.glueSpecified) {
            args.add("--glue");
            args.add("classpath:" + packagePath(clazz));
        }

    }

    private void addStrict(CucumberOptions options, List<String> args) {
        if (options.strict() && !args.contains("--strict")) {
            args.add("--strict");
        }

    }

    private void addIfSet(List<String> args, String value, String key) {
        if (args.contains(value)) {
            return;
        }
        int existingIndex = args.indexOf(key);
        if (existingIndex == -1) {
            args.add(key);
            args.add(value);
        } else {
            args.add(existingIndex + 1, value);
        }
    }

    private void setIfSet(List<String> args, String value, String key) {
        if (args.contains(value)) {
            return;
        }
        int existingIndex = args.indexOf(key);
        if (existingIndex == -1) {
            args.add(key);
            args.add(value);
        } else {
            args.set(existingIndex + 1, value);
        }
    }

    private boolean runningInEnvironmentWithoutAnsiSupport() {
        return System.getProperty("idea.launcher.bin.path") != null;
    }

    private boolean hasSuperClass(Class classWithOptions) {
        return classWithOptions != Object.class;
    }

    private CucumberOptions getOptions(Class<?> clazz) {
        return clazz.getAnnotation(CucumberOptions.class);
    }
}

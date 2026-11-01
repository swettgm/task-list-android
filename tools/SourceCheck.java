import com.sun.source.util.JavacTask;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

/** Offline Java grammar check. This deliberately does not claim Android type checking. */
public final class SourceCheck {
    public static void main(String[] args) throws Exception {
        List<Path> files = new ArrayList<>();
        for (String argument : args) {
            try (var paths = Files.walk(Path.of(argument))) {
                paths.filter(path -> path.toString().endsWith(".java")).forEach(files::add);
            }
        }
        if (files.isEmpty()) throw new IllegalArgumentException("No Java sources found");
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        try (StandardJavaFileManager manager = compiler.getStandardFileManager(diagnostics, null, null)) {
            JavacTask parser = (JavacTask) compiler.getTask(null, manager, diagnostics,
                List.of("-source", "17", "-proc:none"), null, manager.getJavaFileObjectsFromPaths(files));
            for (var ignored : parser.parse()) { }
        }
        boolean failed = false;
        for (var diagnostic : diagnostics.getDiagnostics()) {
            if (diagnostic.getKind() == Diagnostic.Kind.ERROR) {
                System.err.println(diagnostic); failed = true;
            }
        }
        if (failed) System.exit(1);
        System.out.println("Java grammar passed: " + files.size() + " source files. Android compilation remains a separate check.");
    }
}

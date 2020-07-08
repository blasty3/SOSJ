package systemj.common.SOAFacility.Mig;

import static org.objectweb.asm.Type.ARRAY;
import static org.objectweb.asm.Type.OBJECT;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

public class SJClassVisitor extends ClassVisitor {
    private Vector depSet;
	private MethodVisitor mv;
	private SignatureVisitor sv;
	private File classesDirSS;
	private static List<String> excList = new ArrayList<String>(Arrays.asList(
                            "soart","sysj","systemj","org.json","java"
			));
	public SJClassVisitor(int api, File classesDirSS) {
		super(api);
		mv = new SJMethodVisitor(Opcodes.ASM5, this);
		sv = new SJSignatureVisitor(Opcodes.ASM5, this);
                this.depSet = new Vector();
		this.classesDirSS = classesDirSS;
	}
        public SJClassVisitor(int api, File classesDirSS, Vector existingDepList) {
		super(api);
		mv = new SJMethodVisitor(Opcodes.ASM5, this);
		sv = new SJSignatureVisitor(Opcodes.ASM5, this);
                this.depSet = existingDepList;
		this.classesDirSS = classesDirSS;
	}
        public SJClassVisitor(int api, File classesDirSS, String exclCDClassFileName,Vector existingDepList) {
		super(api);
		mv = new SJMethodVisitor(Opcodes.ASM5, this);
		sv = new SJSignatureVisitor(Opcodes.ASM5, this);
                this.depSet = existingDepList;
		this.classesDirSS = classesDirSS;
                excList.add(exclCDClassFileName);
	}
        public Vector getDependencies(){
		return this.depSet;
	}
	public void addDependency(String dep) {
		depSet.add(dep);
	}
	private static boolean needToParse(String p) {
		for (String s : excList) {
			if (p.startsWith(s))
				return false;
		}
		return true;
	}
	public void analyzeDescriptor(Type type) {
		switch (type.getSort()) {
		case OBJECT:
			//log.info("Tries to parse a class " + type.getClassName());
			visitClass(type);
			return;
		case ARRAY:
			analyzeDescriptor(type.getElementType());
			return;
		default:
			return;
		}
	}
	public void visitClass(Type clazzType) {
		String name = clazzType.getClassName();
		String clazzNamePath = name.replace(".", "/");
		if (!depSet.contains(name) && SJClassVisitor.needToParse(name)) {
                    depSet.addElement(name);
			InputStream is = ClassLoader.getSystemResourceAsStream(clazzNamePath + ".class");
			try {
				if (is == null) {
					is = ClassLoader.getSystemResourceAsStream(clazzNamePath + ".java");
					if (is == null)
						throw new RuntimeException("Could not locate a class : " + name);
					StringBuilder sb = new StringBuilder();
					InputStreamReader isr = new InputStreamReader(is, "UTF-8");
					char[] b = new char[1024];
					int len = 0;
					while ((len = isr.read(b)) > 0) {
						sb.append(b, 0, len);
					}
					JavaFileObject jfo = new JavaSource(name, sb.toString());
                                        compileJava(jfo, classesDirSS);
					is.close();
					is = ClassLoader.getSystemResourceAsStream(classesDirSS.getPath() + "/" + clazzNamePath + ".class");
				}
				ClassReader cr = new ClassReader(is);
				cr.accept(this, ClassReader.SKIP_DEBUG);

			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
						e.printStackTrace();
						System.exit(1);
					}
				}
			}
		}
	}
        public static void compileJava(JavaFileObject jfo, File outputDir){
		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
		Iterable<? extends JavaFileObject> units = Arrays.asList(jfo);
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		ArrayList<String> optionList = new ArrayList<String>();
		optionList.addAll(Arrays.asList("-d", outputDir.getPath()));
		JavaCompiler.CompilationTask task = compiler.getTask(null, null, diagnostics, optionList, null, units);
		//boolean result = task.call();
		/*
		if(!result){
			for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
				//log.severe(diagnostic.getKind()+" in "+diagnostic.getSource()+" at line "+ ""+diagnostic.getLineNumber()+"\n"+ diagnostic.getMessage(null));
				System.exit(1);
			}
		}
                */
	}
	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		Type type = Type.getObjectType(name);
		visitClass(type);
		if (interfaces != null) {
			for (String e : interfaces) {
				type = Type.getObjectType(e);
				//log.info("Trying to parse an interface : " + type.getClassName());
				visitClass(type);
			}
		}
		if (signature != null) {
			new SignatureReader(signature).accept(this.sv);
		}
	}
	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		for (Type argType : Type.getArgumentTypes(desc)) {
			analyzeDescriptor(argType);
		}
		{
			Type type = Type.getReturnType(desc);
			analyzeDescriptor(type);
		}
		if (exceptions != null) {
			for (String e : exceptions) {
				Type type = Type.getObjectType(e);
				visitClass(type);
			}
		}
		if (signature != null) {
			new SignatureReader(signature).accept(this.sv);
		}
		return this.mv;
	}
	@Override
	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
		Type type = Type.getType(desc);
		//log.info("Tries to parse a field " + type.getClassName());
		analyzeDescriptor(type);
		if (value instanceof Type) {
			//log.info("Tries to parse initial value of " + name + " " + value);
			analyzeDescriptor(type);
		}

		return null;
	}
	@Override
	public void visitInnerClass(String name, String outerName, String innerName, int access) {
		Type type = Type.getObjectType(name);
		//log.info("Tries to parse inner class " + type.getClassName());
		visitClass(type);
	}
}
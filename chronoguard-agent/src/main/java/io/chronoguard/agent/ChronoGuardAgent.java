package io.chronoguard.agent;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.field.FieldList;
import net.bytebuddy.description.method.MethodList;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.jar.asm.ClassVisitor;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.pool.TypePool;

import java.lang.instrument.Instrumentation;

public class ChronoGuardAgent {

    public static void premain(String args, Instrumentation inst) {
        install(inst);
    }

    public static void agentmain(String args, Instrumentation inst) {
        install(inst);
    }

    private static void install(Instrumentation inst) {
        new AgentBuilder.Default()
            .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
            .ignore(new net.bytebuddy.matcher.ElementMatcher.Junction.AbstractBase<net.bytebuddy.description.type.TypeDescription>() {
                @Override
                public boolean matches(net.bytebuddy.description.type.TypeDescription target) {
                    String name = target.getName();
                    return name.startsWith("java.") ||
                           name.startsWith("javax.") ||
                           name.startsWith("sun.") ||
                           name.startsWith("com.sun.") ||
                           name.startsWith("jdk.") ||
                           name.startsWith("io.chronoguard.") ||
                           name.startsWith("net.bytebuddy") ||
                           name.startsWith("org.junit") ||
                           name.startsWith("org.mockito") ||
                           name.contains("$$Lambda") ||
                           name.contains("GeneratedSerializationConstructorAccessor") ||
                           name.contains("GeneratedMethodAccessor") ||
                           name.contains("GeneratedConstructorAccessor");
                }
            })
            .type(ElementMatchers.not(ElementMatchers.nameStartsWith("io.chronoguard.")))
            .transform((builder, typeDescription, classLoader, module, protectionDomain) ->
                builder.visit(new TimeCallReplacer())
            )
            .installOn(inst);
    }

    static class TimeCallReplacer implements AsmVisitorWrapper {
        @Override
        public int mergeWriter(int flags) {
            return flags;
        }

        @Override
        public int mergeReader(int flags) {
            return flags;
        }

        @Override
        public ClassVisitor wrap(TypeDescription instrumentedType, ClassVisitor classVisitor, Implementation.Context implementationContext, TypePool typePool, FieldList<FieldDescription.InDefinedShape> fields, MethodList<?> methods, int writerFlags, int readerFlags) {
            return new ClassVisitor(Opcodes.ASM9, classVisitor) {
                @Override
                public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                    MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
                    return new MethodVisitor(Opcodes.ASM9, mv) {
                        @Override
                        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                            // Replace System.currentTimeMillis()
                            if (opcode == Opcodes.INVOKESTATIC &&
                                owner.equals("java/lang/System") &&
                                name.equals("currentTimeMillis") &&
                                descriptor.equals("()J")) {
                                super.visitMethodInsn(Opcodes.INVOKESTATIC,
                                    "io/chronoguard/TimeController",
                                    "getCurrentTimeMillis",
                                    "()J",
                                    false);
                            }
                            // Replace Instant.now()
                            else if (opcode == Opcodes.INVOKESTATIC &&
                                owner.equals("java/time/Instant") &&
                                name.equals("now") &&
                                descriptor.equals("()Ljava/time/Instant;")) {
                                super.visitMethodInsn(Opcodes.INVOKESTATIC,
                                    "io/chronoguard/TimeController",
                                    "getCurrentInstant",
                                    "()Ljava/time/Instant;",
                                    false);
                            }
                            // Replace LocalDateTime.now()
                            else if (opcode == Opcodes.INVOKESTATIC &&
                                owner.equals("java/time/LocalDateTime") &&
                                name.equals("now") &&
                                descriptor.equals("()Ljava/time/LocalDateTime;")) {
                                super.visitMethodInsn(Opcodes.INVOKESTATIC,
                                    "io/chronoguard/TimeController",
                                    "getCurrentInstant",
                                    "()Ljava/time/Instant;",
                                    false);
                                super.visitMethodInsn(Opcodes.INVOKESTATIC,
                                    "java/time/ZoneId",
                                    "systemDefault",
                                    "()Ljava/time/ZoneId;",
                                    false);
                                super.visitMethodInsn(Opcodes.INVOKESTATIC,
                                    "java/time/LocalDateTime",
                                    "ofInstant",
                                    "(Ljava/time/Instant;Ljava/time/ZoneId;)Ljava/time/LocalDateTime;",
                                    false);
                            }
                            else {
                                super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
                            }
                        }
                    };
                }
            };
        }
    }
}

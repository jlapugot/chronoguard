package io.chronoguard.agent;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;

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
            .ignore(ElementMatchers.nameStartsWith("io.chronoguard"))
            .ignore(ElementMatchers.nameStartsWith("net.bytebuddy"))
            .type(ElementMatchers.named("java.lang.System"))
            .transform((builder, typeDescription, classLoader, module, protectionDomain) ->
                builder.method(ElementMatchers.named("currentTimeMillis"))
                    .intercept(Advice.to(SystemCurrentTimeMillisAdvice.class))
            )
            .type(ElementMatchers.named("java.time.Clock"))
            .transform((builder, typeDescription, classLoader, module, protectionDomain) ->
                builder.method(ElementMatchers.named("millis"))
                    .intercept(Advice.to(ClockMillisAdvice.class))
            )
            .type(ElementMatchers.named("java.time.Instant"))
            .transform((builder, typeDescription, classLoader, module, protectionDomain) ->
                builder.method(ElementMatchers.named("now").and(ElementMatchers.takesArguments(0)))
                    .intercept(Advice.to(InstantNowAdvice.class))
            )
            .installOn(inst);
    }
    
    public static class SystemCurrentTimeMillisAdvice {
        @Advice.OnMethodExit
        static void exit( @Advice.Return(readOnly = false) long returned) {
            returned = io.chronoguard.TimeController.getCurrentTimeMillis();
        }
    }
    
    public static class ClockMillisAdvice {
        @Advice.OnMethodExit
        static void exit( @Advice.Return(readOnly = false) long returned) {
            returned = io.chronoguard.TimeController.getCurrentTimeMillis();
        }
    }
    
    public static class InstantNowAdvice {
        @Advice.OnMethodExit
        static void exit( @Advice.Return(readOnly = false) java.time.Instant returned) {
            returned = io.chronoguard.TimeController.getCurrentInstant();
        }
    }
}

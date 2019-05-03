package se.wintren.map_processor

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import se.wintren.map_annotation.GenerateClass
import java.io.File
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

@AutoService(Processor::class) // For registering the service
@SupportedSourceVersion(SourceVersion.RELEASE_8) // to support Java 8
@SupportedOptions(GenerateClassProcessor.KAPT_KOTLIN_GENERATED_OPTION_NAME)
class GenerateClassProcessor : AbstractProcessor() {

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(GenerateClass::class.java.name)
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latest()
    }


    override fun process(set: MutableSet<out TypeElement>?, roundEnvironment: RoundEnvironment?): Boolean {
        roundEnvironment?.getElementsAnnotatedWith(GenerateClass::class.java)
            ?.forEach {
                val className = it.simpleName.toString()
                val pack = processingEnv.elementUtils.getPackageOf(it).toString()
                generateClass("Gen_$className", pack)
            }
        return true
    }

    private fun generateClass(className: String, pack: String) {
        val generatedSourcesRoot: String =
            processingEnv.options[BindFieldProcessor.KAPT_KOTLIN_GENERATED_OPTION_NAME].orEmpty()
        if (generatedSourcesRoot.isEmpty()) {
            processingEnv.messager.errormessage { "Can't find the target directory for generated Kotlin files." }
            return
        }


        val fileName = "Gen$className"
        File(generatedSourcesRoot).also {
            it.mkdir()
            FileSpec.builder(pack, fileName)
                .addType(
                    TypeSpec.classBuilder(className)
                        .primaryConstructor(
                            FunSpec.constructorBuilder()
                                .addParameter("name", String::class)
                                .build()
                        )
                        .addProperty(
                            PropertySpec.builder("name", String::class)
                                .initializer("name")
                                .build()
                        )
                        .addFunction(
                            FunSpec.builder("greet")
                                .addStatement("println(%P)", "Hello, \$name")
                                .build()
                        )
                        .build()
                )
                .build()
                .writeTo(it)
        }

    }

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }

}
package se.wintren.map_processor

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import se.wintren.map_annotation.MAP
import java.io.File
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.*
import javax.lang.model.type.DeclaredType


@AutoService(Processor::class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions(MAPProcessor.KAPT_KOTLIN_GENERATED_OPTION_NAME)
class MAPProcessor : AbstractProcessor() {

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }

    private fun print(message: Any) {
        processingEnv.messager.warningMessage { "$message" }
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(MAP::class.java.name)
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latest()
    }

    override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        print("______MAP Processor________")
        roundEnv.getElementsAnnotatedWith(MAP::class.java)
            .filter { it.kind == ElementKind.CLASS || it.kind == ElementKind.INTERFACE }
            .map { it as TypeElement }
            .forEach { typeElement ->
                print("Creating mapper for: ${typeElement.simpleName}")
                createMapperClass(typeElement) { elements ->
                    elements.forEach { element ->
                        when (element) {
                            is ExecutableElement -> {
                                print("_Function")
                                val function = createFunction(element)
                                addFunction(function)
                            }
                            else -> {
                                print("Element [${element.simpleName}] is not Executable= ${element.kind}")
                            }
                        }
                    }
                }
            }
        return true
    }

    private fun createMapperClass(
        typeElement: TypeElement,
        function: TypeSpec.Builder.(elements: List<Element>) -> Unit
    ) {
        val className = "${typeElement.simpleName}Impl"
        val packageName = processingEnv.elementUtils.getPackageOf(typeElement).toString()
        val folder = getFolder() ?: return
        val fileSpec = FileSpec.builder(packageName, className)
        val type = TypeSpec.classBuilder(className).also { classBuilder ->
            function(classBuilder, typeElement.enclosedElements)
        }.build()
        fileSpec.addType(type)
        fileSpec.build().writeTo(folder)
    }

    private fun createFunction(element: ExecutableElement): FunSpec {
        val funSpecBuilder = FunSpec.builder(element.simpleName.toString()).also { function ->
            val returnElement = (element.returnType as DeclaredType).asElement()
            val parameterElement = element.parameters.first()

            // Parameter
            val parameterName = parameterElement.simpleName.toString()
            val parameter = ParameterSpec.builder(parameterName, parameterElement.asType().asTypeName()).build()
            function.addParameter(parameter)

            // Return
            val returnClass = returnElement.simpleName
            function.returns(returnElement.asType().asTypeName())

            // Mapping code
            val parameterType = parameterElement.asType()
            print("kind: ${parameterType.kind}")
            print("kind: ${parameterType.asTypeName()}")
            val typeName = parameterType.asTypeName()

            //** We need to get members from InputType to get their names
//            val members = processingEnv.elementUtils.getAllMembers(typeElement.asType() as TypeElement)

            //** We can nest codeblocks to create the parameter-assignment
            val assignId = CodeBlock.builder().add("""
                |id = dto.id
                """.trimMargin()).build()
            function.addStatement("return %L(\n%L\n)", returnClass, assignId)

            //// Proof of concept, worked fine
//            val dtoName = inParameter.simpleName
//            print("dtoName: $dtoName")
//            val dataModelName = returnType.
//
//            val param1 = "id"
//            val param2 = "title"
//            val param3 = "hasTheCool"
//            val param4 = "coolImageUrl"
//
//            function.addCode( // result not preformatted :P
//                """
//                |return %L(
//                |   %L = dto.%L,
//                |   %L = dto.%L,
//                |   %L = dto.%L,
//                |   %L = dto.%L
//                |)
//                |""".trimMargin(), dataModelName, param1, param1, param2, param2, param3, param3, param4, param4
//            )
        }
        return funSpecBuilder.build()
    }

    private fun getFolder(): File? {
        val generatedSourcesRoot: String =
            processingEnv.options[BindFieldProcessor.KAPT_KOTLIN_GENERATED_OPTION_NAME].orEmpty()
        if (generatedSourcesRoot.isEmpty()) {
            processingEnv.messager.errormessage { "Can't find the target directory for generated Kotlin files." }
            return null
        }
        val file = File(generatedSourcesRoot)
        file.mkdir()
        return file
    }

    // Just keep for reference of element content
    fun Xprocess(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        roundEnv.getElementsAnnotatedWith(MAP::class.java).forEach { classElement ->
            print("______MAP Processor________")
            print(" ")
            print("$annotations")
            print("$roundEnv")
            print("$classElement")
            print("${classElement.enclosedElements}")
            print("${classElement.enclosingElement}")
            print("${classElement.modifiers}")
            print("${classElement.simpleName}")
            print("${classElement.annotationMirrors}")
            classElement.enclosedElements.forEach {
                print("enclosed: ${it}")
                print("kind: ${it.kind}")
                (it as ExecutableElement).also {
                    print("defaultValue: ${it.defaultValue}")
                    print("isDefault: ${it.isDefault}")
                    print("isVarArgs: ${it.isVarArgs}")
                    print("parameters: ${it.parameters}")
                    print("parameters: ${it.parameters}")
                    it.parameters.forEach {
                        print("-  constantValue: ${it.constantValue}")
                        print("-  kind: ${it.kind}")
                        print("-  simpleName: ${it.simpleName}")
                        print("-  asType(): ${it.asType()}")
                        print("-  modifiers: ${it.modifiers}")
                        print("-  enclosedElements: ${it.enclosedElements}")

                    }
                    print("receiverType: ${it.receiverType}")
                    print("returnType: ${it.returnType}")
                    print("thrownTypes: ${it.thrownTypes}")
                    print("kind: ${it.kind}")
                    print("enclosedElements: ${it.enclosedElements}")
                    print("typeParameters: ${it.typeParameters}")
                    print("modifiers: ${it.modifiers}")
                    print("- -")
                }

            }
        }
        return true
    }


}
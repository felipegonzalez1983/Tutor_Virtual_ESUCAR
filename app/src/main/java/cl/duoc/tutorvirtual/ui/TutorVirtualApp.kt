package cl.duoc.tutorvirtual.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.duoc.tutorvirtual.ui.theme.*
import androidx.compose.ui.draw.clip

private data class EtapaInfo(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val objetivo: String,
    val actividades: List<String>
)

private data class Avance(
    val etapaId: Int,
    val titulo: String,
    val descripcion: String,
    val estado: String
)

private val etapas = listOf(
    EtapaInfo(1, "Planteamiento", "Define el problema, la pregunta de investigación y los objetivos.", "Establecer el problema que se investigará y definir los objetivos del trabajo.", listOf("Identificar el problema de investigación.", "Formular la pregunta de investigación.", "Definir los objetivos.")),
    EtapaInfo(2, "Marco Teórico", "Reúne antecedentes, conceptos y fundamentos de la investigación.", "Construir los fundamentos teóricos que respaldan el trabajo.", listOf("Buscar antecedentes relacionados.", "Identificar conceptos importantes.", "Organizar los fundamentos teóricos.")),
    EtapaInfo(3, "Metodología", "Describe cómo se desarrollará la investigación y qué métodos se utilizarán.", "Definir el procedimiento y los métodos que se utilizarán en la investigación.", listOf("Definir el enfoque de investigación.", "Seleccionar los métodos de trabajo.", "Describir el procedimiento.")),
    EtapaInfo(4, "Recogida de información", "Organiza la obtención de los datos necesarios para la investigación.", "Obtener y organizar la información necesaria para desarrollar el trabajo.", listOf("Identificar las fuentes de información.", "Recopilar los datos necesarios.", "Organizar la información obtenida.")),
    EtapaInfo(5, "Análisis de resultados", "Examina e interpreta la información obtenida.", "Interpretar la información recopilada y examinar los resultados.", listOf("Revisar los datos recopilados.", "Identificar resultados relevantes.", "Interpretar los resultados."))
)

private enum class Seccion { INICIO, ETAPAS, DETALLE, FORMULARIO, PROGRESO, FAQ, HITOS, RUBRICAS }

@Composable
fun TutorVirtualApp(modifier: Modifier = Modifier) {
    var seccion by rememberSaveable { mutableStateOf(Seccion.INICIO.name) }
    var etapaId by rememberSaveable { mutableIntStateOf(1) }
    val avances = remember { mutableStateListOf<Avance>() }
    val etapaActual = etapas.first { it.id == etapaId }
    val current = Seccion.valueOf(seccion)
    val completadas = etapas.count { etapa -> avances.any { it.etapaId == etapa.id && it.estado == "Completado" } }

    Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = MarfilESUCAR,
            bottomBar = {
                if (current == Seccion.INICIO || current == Seccion.ETAPAS || current == Seccion.PROGRESO || current == Seccion.FAQ || current == Seccion.HITOS || current == Seccion.RUBRICAS) {
                    NavigationBar(containerColor = VerdeESUCAR, contentColor = Blanco) {
                        val tabs = listOf(
                            Triple("Inicio", Icons.Default.Home, Seccion.INICIO),
                            Triple("Etapas", Icons.Default.Layers, Seccion.ETAPAS),
                            Triple("Progreso", Icons.Default.BarChart, Seccion.PROGRESO),
                            Triple("Más", Icons.Default.MoreHoriz, Seccion.FAQ)
                        )
                        tabs.forEach { (label, icon, destination) ->
                            NavigationBarItem(
                                selected = current == destination || (label == "Más" && current in listOf(Seccion.FAQ, Seccion.HITOS, Seccion.RUBRICAS)),
                                onClick = { seccion = destination.name },
                                icon = { Icon(icon, contentDescription = label) },
                                label = { Text(label) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = DoradoESUCAR,
                                    selectedTextColor = Blanco,
                                    indicatorColor = GrisOscuroESUCAR,
                                    unselectedIconColor = Blanco,
                                    unselectedTextColor = Blanco
                                )
                            )
                        }
                    }
                }
            }
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                when (current) {
                    Seccion.INICIO -> InicioContent(
                        completadas = completadas,
                        onNavigate = { seccion = it.name }
                    )
                    Seccion.ETAPAS -> ListadoContent(
                        avances = avances,
                        onBack = { seccion = Seccion.INICIO.name },
                        onSelect = { etapaId = it; seccion = Seccion.DETALLE.name }
                    )
                    Seccion.DETALLE -> DetalleContent(
                        etapa = etapaActual,
                        avance = avances.lastOrNull { it.etapaId == etapaId },
                        onBack = { seccion = Seccion.ETAPAS.name },
                        onRegister = { seccion = Seccion.FORMULARIO.name }
                    )
                    Seccion.FORMULARIO -> FormularioContent(
                        etapa = etapaActual,
                        onBack = { seccion = Seccion.DETALLE.name },
                        onSave = { avance ->
                            val oldIndex = avances.indexOfLast { it.etapaId == etapaId }
                            if (oldIndex >= 0) avances[oldIndex] = avance else avances.add(avance)
                            seccion = Seccion.DETALLE.name
                        }
                    )
                    Seccion.PROGRESO -> ProgresoContent(avances, completadas)
                    Seccion.FAQ -> MasContent(
                        onHitos = { seccion = Seccion.HITOS.name },
                        onRubricas = { seccion = Seccion.RUBRICAS.name }
                    )
                    Seccion.HITOS -> InfoContent("Hitos y entregas", "Organiza tus entregas y revisa los hitos de tu trabajo de título.", listOf("Planteamiento: fecha por definir", "Marco Teórico: fecha por definir", "Metodología: fecha por definir", "Recogida de información: fecha por definir", "Análisis de resultados: fecha por definir"), onBack = { seccion = Seccion.FAQ.name })
                    Seccion.RUBRICAS -> InfoContent("Pautas y rúbricas", "Consulta las pautas que entregue tu equipo docente. Esta sección queda preparada para incorporar los documentos oficiales.", listOf("Pauta de evaluación: pendiente de incorporar", "Rúbrica de trabajo de título: pendiente de incorporar"), onBack = { seccion = Seccion.FAQ.name })
                }
            }
        }
}

@Composable
private fun Encabezado(titulo: String, subtitulo: String? = null, onBack: (() -> Unit)? = null) {
    Row(
        Modifier.fillMaxWidth().background(VerdeESUCAR).padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver a la pantalla anterior", tint = Blanco)
            }
        } else {
            Icon(Icons.Default.School, contentDescription = "Identidad institucional ESUCAR", tint = DoradoESUCAR, modifier = Modifier.size(42.dp).padding(4.dp))
        }
        Column(Modifier.padding(start = 8.dp)) {
            Text(titulo, color = Blanco, fontSize = 19.sp, fontWeight = FontWeight.Bold)
            if (subtitulo != null) Text(subtitulo, color = Blanco, fontSize = 12.sp)
        }
    }
}

@Composable
private fun InicioContent(completadas: Int, onNavigate: (Seccion) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MarfilESUCAR)
    ) {
        // ENCABEZADO INSTITUCIONAL: se conserva la composición original.
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp),
            colors = CardDefaults.cardColors(containerColor = VerdeESUCAR),
            elevation = CardDefaults.cardElevation(3.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 22.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = "Identidad institucional ESUCAR",
                    tint = DoradoESUCAR,
                    modifier = Modifier.size(55.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text("ESUCAR", color = Blanco, fontSize = 23.sp, fontWeight = FontWeight.Bold)
                    Text("Escuela de Suboficiales", color = Blanco, fontSize = 13.sp)
                    Text("Carabineros de Chile", color = Blanco.copy(alpha = 0.85f), fontSize = 11.sp)
                }
            }
        }

        // CONTENIDO PRINCIPAL: misma distribución, tamaños y separaciones del original.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(17.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = VerdeESUCAR,
                    modifier = Modifier.size(43.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Tutor Virtual", color = VerdeESUCAR, fontSize = 25.sp, fontWeight = FontWeight.Bold)
                    Text("Trabajo de Título I - III", color = GrisOscuroESUCAR, fontSize = 13.sp)
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("¡Hola, Estudiante!", color = VerdeESUCAR, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Gestiona tu trabajo de título, avanza por etapas y resuelve tus dudas aquí.",
                    color = GrisOscuroESUCAR,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Blanco),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.BarChart, contentDescription = null, tint = VerdeESUCAR)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Mi progreso", color = GrisOscuroESUCAR, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Text("$completadas de 5 etapas", color = GrisOscuroESUCAR, fontSize = 11.sp)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(9.dp)
                            .clip(CircleShape)
                            .background(VerdeClaro)
                            .semantics {
                                contentDescription = "Progreso del trabajo de título: ${completadas * 20} por ciento"
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(completadas / 5f)
                                .fillMaxHeight()
                                .background(DoradoESUCAR)
                        )
                    }

                    Text(
                        "${completadas * 20}%",
                        color = VerdeESUCAR,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }

            Text("¿Qué necesitas hacer?", color = GrisOscuroESUCAR, fontWeight = FontWeight.Bold, fontSize = 16.sp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AccesoOriginal("Etapas\nde la Tesis", Icons.Default.Layers, Modifier.weight(1f)) {
                    onNavigate(Seccion.ETAPAS)
                }
                AccesoOriginal("Preguntas\nFrecuentes", Icons.Default.Help, Modifier.weight(1f)) {
                    onNavigate(Seccion.FAQ)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AccesoOriginal("Hitos y\nEntregas", Icons.Default.CalendarMonth, Modifier.weight(1f)) {
                    onNavigate(Seccion.HITOS)
                }
                AccesoOriginal("Pautas y\nRúbricas", Icons.Default.MenuBook, Modifier.weight(1f)) {
                    onNavigate(Seccion.RUBRICAS)
                }
            }
        }
    }
}

@Composable
private fun AccesoOriginal(
    titulo: String,
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(112.dp)
            .clickable(onClick = onClick)
            .semantics { contentDescription = titulo.replace("\n", " ") },
        shape = RoundedCornerShape(17.dp),
        colors = CardDefaults.cardColors(containerColor = VerdeClaro),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icono, contentDescription = null, tint = VerdeESUCAR, modifier = Modifier.size(31.dp))
            Spacer(modifier = Modifier.height(7.dp))
            Text(
                text = titulo,
                color = GrisOscuroESUCAR,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun ListadoContent(avances: List<Avance>, onBack: () -> Unit, onSelect: (Int) -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Encabezado("Etapas de la Tesis", "Guía metodológica", onBack)
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Conoce las etapas que debes desarrollar durante tu trabajo de título.", color = GrisTexto)
            etapas.forEach { etapa ->
                val estado = avances.lastOrNull { it.etapaId == etapa.id }?.estado ?: "Por iniciar"
                Card(
                    Modifier.fillMaxWidth().clickable { onSelect(etapa.id) },
                    colors = CardDefaults.cardColors(containerColor = Blanco),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(Modifier.size(44.dp), shape = RoundedCornerShape(12.dp), color = VerdeClaro) {
                            Box(contentAlignment = Alignment.Center) { Text("${etapa.id}", color = VerdeESUCAR, fontWeight = FontWeight.Bold, fontSize = 18.sp) }
                        }
                        Column(Modifier.weight(1f).padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text(etapa.nombre, color = GrisOscuroESUCAR, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(etapa.descripcion, color = GrisTexto, fontSize = 13.sp, lineHeight = 18.sp)
                            Text(estado, color = VerdeESUCAR, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = "Abrir ${etapa.nombre}", tint = DoradoESUCAR)
                    }
                }
            }
        }
    }
}

@Composable
private fun DetalleContent(etapa: EtapaInfo, avance: Avance?, onBack: () -> Unit, onRegister: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Encabezado("Detalle de etapa", "Etapa ${etapa.id} de 5", onBack)
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(etapa.nombre, color = VerdeESUCAR, fontSize = 25.sp, fontWeight = FontWeight.Bold)
            SeccionCard("Objetivo", etapa.objetivo)
            SeccionCard("Descripción", etapa.descripcion)
            Card(colors = CardDefaults.cardColors(containerColor = Blanco), shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("¿Qué debes desarrollar?", color = VerdeESUCAR, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    etapa.actividades.forEach { actividad ->
                        Text("•  $actividad", color = GrisOscuroESUCAR, lineHeight = 20.sp)
                    }
                }
            }
            SeccionCard("Información de entrega", "Formato requerido: por definir\nFecha límite: por definir\nEstado: ${avance?.estado ?: "Por iniciar"}")
            if (avance != null) SeccionCard("Último avance registrado", "${avance.titulo}\n\n${avance.descripcion}\n\nEstado: ${avance.estado}")
            Button(onClick = onRegister, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp), colors = ButtonDefaults.buttonColors(containerColor = VerdeESUCAR)) {
                Icon(Icons.Default.Edit, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (avance == null) "Registrar avance" else "Actualizar avance")
            }
        }
    }
}

@Composable
private fun SeccionCard(titulo: String, contenido: String) {
    Card(colors = CardDefaults.cardColors(containerColor = Blanco), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(titulo, color = VerdeESUCAR, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            Text(contenido, color = GrisOscuroESUCAR, lineHeight = 21.sp)
        }
    }
}

@Composable
private fun FormularioContent(etapa: EtapaInfo, onBack: () -> Unit, onSave: (Avance) -> Unit) {
    var titulo by rememberSaveable(etapa.id) { mutableStateOf("") }
    var descripcion by rememberSaveable(etapa.id) { mutableStateOf("") }
    var estado by rememberSaveable(etapa.id) { mutableStateOf("En progreso") }
    var intentoGuardar by rememberSaveable(etapa.id) { mutableStateOf(false) }
    val tituloError = intentoGuardar && titulo.trim().isEmpty()
    val descripcionError = intentoGuardar && descripcion.trim().length < 10

    Column(Modifier.fillMaxSize()) {
        Encabezado("Registrar avance", etapa.nombre, onBack)
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).imePadding().padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Completa los datos del trabajo realizado. Los campos marcados son obligatorios.", color = GrisTexto)
            OutlinedTextField(
                value = titulo,
                onValueChange = { titulo = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Nombre del avance *") },
                placeholder = { Text("Ej. Definición del problema") },
                singleLine = true,
                isError = tituloError,
                supportingText = { if (tituloError) Text("El nombre del avance es obligatorio.") else Text("Escribe un título breve y claro.") },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Sentences)
            )
            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                modifier = Modifier.fillMaxWidth().heightIn(min = 130.dp),
                label = { Text("Descripción del avance *") },
                placeholder = { Text("Describe qué has desarrollado...") },
                minLines = 4,
                isError = descripcionError,
                supportingText = { if (descripcionError) Text("Escribe al menos 10 caracteres.") else Text("${descripcion.length} caracteres") }
            )
            Text("Estado del avance *", color = GrisOscuroESUCAR, fontWeight = FontWeight.SemiBold)
            listOf("Por iniciar", "En progreso", "Completado").forEach { opcion ->
                Row(
                    Modifier.fillMaxWidth().clickable { estado = opcion }.semantics { contentDescription = "Estado $opcion" }.padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = estado == opcion, onClick = { estado = opcion })
                    Text(opcion, color = GrisOscuroESUCAR)
                }
            }
            if (intentoGuardar && tituloError || intentoGuardar && descripcionError) {
                Text("Revisa los campos indicados antes de guardar.", color = RojoError, fontWeight = FontWeight.Medium)
            }
            Button(
                onClick = {
                    intentoGuardar = true
                    if (titulo.trim().isNotEmpty() && descripcion.trim().length >= 10) {
                        onSave(Avance(etapa.id, titulo.trim(), descripcion.trim(), estado))
                    }
                },
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VerdeESUCAR)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Guardar avance")
            }
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp)) {
                Text("Cancelar")
            }
            Text("Los avances se mantienen mientras la aplicación permanezca abierta. La sincronización o persistencia definitiva requiere integración adicional.", color = GrisTexto, fontSize = 12.sp)
        }
    }
}

@Composable
private fun ProgresoContent(avances: List<Avance>, completadas: Int) {
    Column(Modifier.fillMaxSize()) {
        Encabezado("Mi progreso", "Resumen de trabajo de título")
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("$completadas de 5 etapas completadas", color = VerdeESUCAR, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            LinearProgressIndicator(progress = { completadas / 5f }, modifier = Modifier.fillMaxWidth().height(12.dp), color = DoradoESUCAR, trackColor = VerdeClaro)
            Text("${completadas * 20}% de avance", color = GrisTexto)
            etapas.forEach { etapa ->
                val avance = avances.lastOrNull { it.etapaId == etapa.id }
                SeccionCard(etapa.nombre, "Estado: ${avance?.estado ?: "Por iniciar"}${if (avance != null) "\nAvance: ${avance.titulo}" else ""}")
            }
        }
    }
}

@Composable
private fun MasContent(onHitos: () -> Unit, onRubricas: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Encabezado("Recursos de apoyo", "Información para el estudiante")
        Column(Modifier.fillMaxSize().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Acceso("Hitos y entregas", Icons.Default.CalendarMonth, Modifier.fillMaxWidth()) { onHitos() }
            Acceso("Pautas y rúbricas", Icons.Default.MenuBook, Modifier.fillMaxWidth()) { onRubricas() }
            SeccionCard("Preguntas frecuentes", "¿Cómo avanzo por las etapas?\nSelecciona una etapa para consultar su objetivo y registrar tu avance.\n\n¿Puedo modificar un avance?\nSí. Ingresa nuevamente a la etapa y selecciona Actualizar avance.")
            SeccionCard("Ayuda", "Si tienes dudas sobre fechas, formatos o criterios de evaluación, consulta las instrucciones oficiales de tu docente.")
        }
    }
}

@Composable
private fun InfoContent(titulo: String, descripcion: String, items: List<String>, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Encabezado(titulo, "Recursos de apoyo", onBack)
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(descripcion, color = GrisTexto, lineHeight = 21.sp)
            items.forEach { SeccionCard("Información", it) }
        }
    }
}

@Composable
private fun Acceso(
    titulo: String,
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(112.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(17.dp),
        colors = CardDefaults.cardColors(
            containerColor = VerdeClaro
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = VerdeESUCAR,
                modifier = Modifier.size(31.dp)
            )

            Spacer(modifier = Modifier.height(7.dp))

            Text(
                text = titulo,
                color = GrisOscuroESUCAR,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
        }
    }
}
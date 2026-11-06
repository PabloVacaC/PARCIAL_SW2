class CodeFileModel {
  final String ruta;
  final String nombre;
  final String tipo; // JAVA, DART, SQL, YAML, JSON, MARKDOWN, TEXT
  final String contenido;

  CodeFileModel({
    required this.ruta,
    required this.nombre,
    required this.tipo,
    required this.contenido,
  });

  factory CodeFileModel.fromJson(Map<String, dynamic> json) {
    return CodeFileModel(
      ruta: json['ruta'] ?? '',
      nombre: json['nombre'] ?? 'Archivo',
      tipo: json['tipo'] ?? 'TEXT',
      contenido: json['contenido'] ?? '',
    );
  }
}

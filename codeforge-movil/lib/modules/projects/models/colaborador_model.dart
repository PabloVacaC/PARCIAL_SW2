class ColaboradorModel {
  final int idColaborador;
  final int idUsuario;
  final String username;
  final String nombreCompleto;
  final String rol;
  final String? fechaUnion;

  ColaboradorModel({
    required this.idColaborador,
    required this.idUsuario,
    required this.username,
    required this.nombreCompleto,
    required this.rol,
    this.fechaUnion,
  });

  factory ColaboradorModel.fromJson(Map<String, dynamic> json) {
    return ColaboradorModel(
      idColaborador: json['idColaborador'] is int
          ? json['idColaborador']
          : int.tryParse(json['idColaborador']?.toString() ?? '0') ?? 0,
      idUsuario: json['idUsuario'] is int
          ? json['idUsuario']
          : int.tryParse(json['idUsuario']?.toString() ?? '0') ?? 0,
      username: json['username'] ?? '',
      nombreCompleto: json['nombreCompleto'] ?? json['nombre'] ?? '',
      rol: json['rolProyecto'] ?? json['rol'] ?? 'COLABORADOR',
      fechaUnion: json['fechaUnion']?.toString(),
    );
  }
}

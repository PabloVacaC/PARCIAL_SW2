class AuthUser {
  final int idUsuario;
  final String nombre;
  final String apellido;
  final String username;
  final String correo;
  final String estado;

  AuthUser({
    required this.idUsuario,
    required this.nombre,
    required this.apellido,
    required this.username,
    required this.correo,
    required this.estado,
  });

  String get nombreCompleto => '$nombre $apellido'.trim();

  factory AuthUser.fromJson(Map<String, dynamic> json) {
    return AuthUser(
      idUsuario: json['idUsuario'] is int
          ? json['idUsuario']
          : int.tryParse(json['idUsuario']?.toString() ?? '0') ?? 0,
      nombre: json['nombre'] ?? '',
      apellido: json['apellido'] ?? '',
      username: json['username'] ?? '',
      correo: json['correo'] ?? '',
      estado: json['estado'] ?? 'ACTIVO',
    );
  }

  Map<String, dynamic> toJson() => {
        'idUsuario': idUsuario,
        'nombre': nombre,
        'apellido': apellido,
        'username': username,
        'correo': correo,
        'estado': estado,
      };
}

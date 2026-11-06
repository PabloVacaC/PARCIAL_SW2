class ApiException implements Exception {
  final String mensaje;
  final int? codigoEstado;

  ApiException(this.mensaje, {this.codigoEstado});

  @override
  String toString() => mensaje;
}

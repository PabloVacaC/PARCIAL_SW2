import 'auth_user.dart';

class LoginResponse {
  final String token;
  final String? mensaje;
  final AuthUser usuario;

  LoginResponse({
    required this.token,
    this.mensaje,
    required this.usuario,
  });

  factory LoginResponse.fromJson(Map<String, dynamic> json) {
    return LoginResponse(
      token: json['token'] ?? '',
      mensaje: json['mensaje'],
      usuario: AuthUser.fromJson(json),
    );
  }
}

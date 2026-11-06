import 'package:flutter/material.dart';

class AppColors {
  // Fondos y superficies
  static const Color background = Color(0xFF090D16);
  static const Color surface = Color(0xFF111827);
  static const Color card = Color(0xFF1E293B);
  static const Color cardElevated = Color(0xFF283548);
  
  // Bordes y divisores
  static const Color border = Color(0xFF334155);
  static const Color borderLight = Color(0xFF475569);
  
  // Colores primarios y acentos
  static const Color primary = Color(0xFF6366F1); // Indigo moderno
  static const Color primaryLight = Color(0xFF818CF8);
  static const Color primaryDark = Color(0xFF4F46E5);
  
  static const Color secondary = Color(0xFF8B5CF6); // Violeta
  static const Color accent = Color(0xFF06B6D4); // Cyan resplandeciente
  
  // Indicador de Modo Solo Lectura
  static const Color readOnlyPrimary = Color(0xFF3B82F6);
  static const Color readOnlyBadgeBg = Color(0x1F3B82F6);
  static const Color readOnlyBadgeBorder = Color(0x4D3B82F6);

  // Estados
  static const Color success = Color(0xFF10B981); // Emerald
  static const Color successBg = Color(0x1A10B981);
  static const Color warning = Color(0xFFF59E0B); // Amber
  static const Color warningBg = Color(0x1AF59E0B);
  static const Color error = Color(0xFFEF4444); // Rose red
  static const Color errorBg = Color(0x1AEF4444);
  static const Color info = Color(0xFF0284C7); // Sky

  // Textos y contrastes legibles
  static const Color textPrimary = Color(0xFFF8FAFC);
  static const Color textSecondary = Color(0xFF94A3B8);
  static const Color textMuted = Color(0xFF64748B);
  static const Color textInverse = Color(0xFF090D16);

  // Gradientes de marca CodeForge
  static const LinearGradient brandGradient = LinearGradient(
    colors: [Color(0xFF6366F1), Color(0xFF8B5CF6)],
    begin: Alignment.topLeft,
    end: Alignment.bottomRight,
  );

  static const LinearGradient cardGlow = LinearGradient(
    colors: [Color(0x1A6366F1), Color(0x05111827)],
    begin: Alignment.topLeft,
    end: Alignment.bottomRight,
  );
}

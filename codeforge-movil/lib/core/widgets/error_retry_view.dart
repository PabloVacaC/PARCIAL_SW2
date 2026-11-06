import 'package:flutter/material.dart';
import '../theme/app_colors.dart';
import '../theme/app_typography.dart';
import 'custom_button.dart';

class ErrorRetryView extends StatelessWidget {
  final String mensaje;
  final VoidCallback onRetry;

  const ErrorRetryView({
    super.key,
    required this.mensaje,
    required this.onRetry,
  });

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(32.0),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Container(
              width: 72,
              height: 72,
              decoration: BoxDecoration(
                color: AppColors.errorBg,
                shape: BoxShape.circle,
                border: Border.all(color: AppColors.error.withAlpha(80), width: 1.5),
              ),
              child: const Icon(Icons.wifi_off_rounded, size: 34, color: AppColors.error),
            ),
            const SizedBox(height: 20),
            Text('No se pudo cargar la información',
                style: AppTypography.h3, textAlign: TextAlign.center),
            const SizedBox(height: 10),
            Text(
              mensaje,
              style: AppTypography.bodyMedium,
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 24),
            SizedBox(
              width: 170,
              child: CustomButton(
                text: 'Reintentar',
                icon: Icons.refresh_rounded,
                onPressed: onRetry,
              ),
            ),
          ],
        ),
      ),
    );
  }
}

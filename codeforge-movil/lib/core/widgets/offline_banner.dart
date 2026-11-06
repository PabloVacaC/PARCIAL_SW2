import 'package:flutter/material.dart';
import '../network/offline_state.dart';
import '../theme/app_colors.dart';
import '../theme/app_typography.dart';

class OfflineBanner extends StatelessWidget {
  final VoidCallback? onReintentar;

  const OfflineBanner({super.key, this.onReintentar});

  @override
  Widget build(BuildContext context) {
    return ListenableBuilder(
      listenable: OfflineState.instance,
      builder: (context, _) {
        final state = OfflineState.instance;

        if (state.mostrandoRestaurado) {
          return AnimatedContainer(
            duration: const Duration(milliseconds: 300),
            width: double.infinity,
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
            decoration: BoxDecoration(
              color: AppColors.success.withAlpha(45),
              border: Border(
                bottom: BorderSide(color: AppColors.success.withAlpha(120), width: 1),
              ),
            ),
            child: Row(
              children: [
                const Icon(Icons.wifi_rounded, size: 16, color: AppColors.success),
                const SizedBox(width: 8),
                Expanded(
                  child: Text(
                    'Conexión restablecida · Sincronizando datos con el servidor...',
                    style: AppTypography.bodySmall.copyWith(
                      color: AppColors.success,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                ),
              ],
            ),
          );
        }

        if (state.isOffline) {
          return AnimatedContainer(
            duration: const Duration(milliseconds: 300),
            width: double.infinity,
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
            decoration: BoxDecoration(
              color: AppColors.warning.withAlpha(40),
              border: Border(
                bottom: BorderSide(color: AppColors.warning.withAlpha(120), width: 1),
              ),
            ),
            child: Row(
              children: [
                const Icon(Icons.cloud_off_rounded, size: 16, color: AppColors.warning),
                const SizedBox(width: 8),
                Expanded(
                  child: Text(
                    'Modo sin conexión · Visualizando datos guardados localmente',
                    style: AppTypography.bodySmall.copyWith(
                      color: AppColors.warning,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                ),
                if (onReintentar != null) ...[
                  const SizedBox(width: 6),
                  InkWell(
                    onTap: onReintentar,
                    borderRadius: BorderRadius.circular(6),
                    child: Padding(
                      padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                      child: Row(
                        children: [
                          const Icon(Icons.refresh_rounded, size: 14, color: AppColors.warning),
                          const SizedBox(width: 4),
                          Text(
                            'Reconectar',
                            style: AppTypography.bodySmall.copyWith(
                              color: AppColors.warning,
                              fontWeight: FontWeight.bold,
                              decoration: TextDecoration.underline,
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),
                ],
              ],
            ),
          );
        }

        return const SizedBox.shrink();
      },
    );
  }
}


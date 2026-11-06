import 'package:flutter/material.dart';
import '../theme/app_colors.dart';
import '../theme/app_typography.dart';

class ReadOnlyBadge extends StatelessWidget {
  final bool compact;

  const ReadOnlyBadge({super.key, this.compact = false});

  @override
  Widget build(BuildContext context) {
    if (compact) {
      return Container(
        padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
        decoration: BoxDecoration(
          color: AppColors.readOnlyBadgeBg,
          borderRadius: BorderRadius.circular(20),
          border: Border.all(color: AppColors.readOnlyBadgeBorder, width: 1),
        ),
        child: Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            const Icon(Icons.visibility_outlined, size: 12, color: AppColors.readOnlyPrimary),
            const SizedBox(width: 4),
            Text(
              'SOLO LECTURA',
              style: AppTypography.bodySmall.copyWith(
                color: AppColors.readOnlyPrimary,
                fontWeight: FontWeight.w700,
                fontSize: 9.5,
                letterSpacing: 0.5,
              ),
            ),
          ],
        ),
      );
    }

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
      decoration: BoxDecoration(
        color: AppColors.readOnlyBadgeBg,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: AppColors.readOnlyBadgeBorder, width: 1),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          const Icon(Icons.remove_red_eye_outlined, size: 15, color: AppColors.readOnlyPrimary),
          const SizedBox(width: 6),
          Text(
            'Modo Solo Lectura',
            style: AppTypography.label.copyWith(
              color: AppColors.readOnlyPrimary,
              fontWeight: FontWeight.w600,
            ),
          ),
        ],
      ),
    );
  }
}

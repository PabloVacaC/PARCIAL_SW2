import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../../core/services/proposal_monitor_service.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_typography.dart';
import '../../../core/widgets/empty_state_view.dart';
import '../../../core/widgets/error_retry_view.dart';
import '../../../core/widgets/offline_banner.dart';
import '../../../core/widgets/read_only_badge.dart';
import '../../../core/widgets/skeleton_loader.dart';
import '../../auth/providers/auth_provider.dart';
import '../../project_detail/views/project_detail_view.dart';
import '../providers/proyecto_provider.dart';
import 'widgets/proyecto_card.dart';

class ProyectosListView extends StatefulWidget {
  const ProyectosListView({super.key});

  @override
  State<ProyectosListView> createState() => _ProyectosListViewState();
}

class _ProyectosListViewState extends State<ProyectosListView> {
  final _searchController = TextEditingController();

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) async {
      final provider = context.read<ProyectoProvider>();
      await provider.cargarProyectos();
      if (mounted) {
        ProposalMonitorService.instance.iniciarMonitoreo(
          idsProyectos: provider.proyectos.map((p) => p.idProyecto).toList(),
        );
      }
    });
  }

  @override
  void dispose() {
    _searchController.dispose();
    super.dispose();
  }

  void _confirmarCerrarSesion() {
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        backgroundColor: AppColors.card,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
        title: Text('Cerrar Sesión', style: AppTypography.h3),
        content: Text(
          '¿Estás seguro de que deseas cerrar sesión en la aplicación móvil?',
          style: AppTypography.bodyMedium,
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: Text('Cancelar', style: AppTypography.label.copyWith(color: AppColors.textMuted)),
          ),
          ElevatedButton(
            style: ElevatedButton.styleFrom(
              backgroundColor: AppColors.error,
              foregroundColor: Colors.white,
            ),
            onPressed: () {
              Navigator.pop(ctx);
              context.read<AuthProvider>().logout();
            },
            child: const Text('Cerrar Sesión'),
          ),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final authProvider = context.watch<AuthProvider>();
    final proyectoProvider = context.watch<ProyectoProvider>();
    final usuario = authProvider.usuario;

    return Scaffold(
      appBar: AppBar(
        titleSpacing: 20,
        title: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Text('CodeForge', style: AppTypography.h2),
                const SizedBox(width: 8),
                const ReadOnlyBadge(compact: true),
              ],
            ),
            if (usuario != null)
              Text(
                'Hola, ${usuario.nombreCompleto}',
                style: AppTypography.bodySmall,
              ),
          ],
        ),
        actions: [
          IconButton(
            tooltip: 'Probar Notificación IA',
            icon: const Icon(Icons.notifications_active_outlined, color: AppColors.primaryLight),
            onPressed: () {
              final proyId = proyectoProvider.proyectos.isNotEmpty
                  ? proyectoProvider.proyectos.first.idProyecto
                  : 1;
              ProposalMonitorService.instance.simularNotificacionPrueba(idProyecto: proyId);
            },
          ),
          IconButton(
            tooltip: 'Cerrar Sesión',
            icon: const Icon(Icons.logout_rounded, color: AppColors.textSecondary),
            onPressed: () {
              ProposalMonitorService.instance.reiniciar();
              _confirmarCerrarSesion();
            },
          ),
          const SizedBox(width: 8),
        ],
      ),
      body: RefreshIndicator(
        color: AppColors.primary,
        backgroundColor: AppColors.card,
        onRefresh: () => proyectoProvider.cargarProyectos(),
        child: Column(
          children: [
            // Indicador de modo sin conexión
            OfflineBanner(
              onReintentar: () => proyectoProvider.cargarProyectos(),
            ),

            // Barra de búsqueda de solo lectura
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 12),
              child: TextField(
                controller: _searchController,
                onChanged: (v) => proyectoProvider.filtrar(v),
                style: AppTypography.bodyLarge,
                decoration: InputDecoration(
                  hintText: 'Buscar proyectos autorizados...',
                  prefixIcon: const Icon(Icons.search_rounded, color: AppColors.textMuted),
                  suffixIcon: _searchController.text.isNotEmpty
                      ? IconButton(
                          icon: const Icon(Icons.close_rounded, color: AppColors.textMuted),
                          onPressed: () {
                            _searchController.clear();
                            proyectoProvider.filtrar('');
                          },
                        )
                      : null,
                ),
              ),
            ),

            // Contenido principal
            Expanded(
              child: Builder(
                builder: (context) {
                  if (proyectoProvider.cargando && proyectoProvider.proyectos.isEmpty) {
                    return ListView.separated(
                      padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 8),
                      itemCount: 4,
                      separatorBuilder: (_, _) => const SizedBox(height: 16),
                      itemBuilder: (_, _) => Container(
                        height: 160,
                        padding: const EdgeInsets.all(16),
                        decoration: BoxDecoration(
                          color: AppColors.card,
                          borderRadius: BorderRadius.circular(16),
                          border: Border.all(color: AppColors.border),
                        ),
                        child: const Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            SkeletonLoader(width: 140, height: 18),
                            SizedBox(height: 12),
                            SkeletonLoader(width: double.infinity, height: 14),
                            SizedBox(height: 8),
                            SkeletonLoader(width: 200, height: 14),
                            Spacer(),
                            SkeletonLoader(width: double.infinity, height: 4),
                          ],
                        ),
                      ),
                    );
                  }

                  if (proyectoProvider.error != null && proyectoProvider.proyectos.isEmpty) {
                    return ErrorRetryView(
                      mensaje: proyectoProvider.error!,
                      onRetry: () => proyectoProvider.cargarProyectos(),
                    );
                  }

                  if (proyectoProvider.proyectos.isEmpty) {
                    return EmptyStateView(
                      icon: Icons.folder_open_rounded,
                      title: 'No se encontraron proyectos',
                      description: proyectoProvider.filtro.isNotEmpty
                          ? 'No hay proyectos autorizados que coincidan con "${proyectoProvider.filtro}".'
                          : 'Aún no tienes proyectos asignados en tu cuenta web de CodeForge.',
                    );
                  }

                  return ListView.separated(
                    padding: const EdgeInsets.only(left: 20, right: 20, top: 8, bottom: 24),
                    itemCount: proyectoProvider.proyectos.length,
                    separatorBuilder: (_, _) => const SizedBox(height: 16),
                    itemBuilder: (context, index) {
                      final proyecto = proyectoProvider.proyectos[index];
                      return ProyectoCard(
                        proyecto: proyecto,
                        onTap: () {
                          Navigator.push(
                            context,
                            MaterialPageRoute(
                              builder: (_) => ProjectDetailView(proyecto: proyecto),
                            ),
                          );
                        },
                      );
                    },
                  );
                },
              ),
            ),
          ],
        ),
      ),
    );
  }
}

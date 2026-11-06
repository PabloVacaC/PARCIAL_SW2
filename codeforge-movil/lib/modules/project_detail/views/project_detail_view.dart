import 'package:flutter/material.dart';
import '../../../core/services/proposal_monitor_service.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_typography.dart';
import '../../../core/widgets/offline_banner.dart';
import '../../../core/widgets/read_only_badge.dart';
import '../../documentation/views/documentation_view.dart';
import '../../generated_code/views/code_browser_view.dart';
import '../../ia_chat/views/ia_history_view.dart';
import '../../projects/models/proyecto_model.dart';
import '../../proposals/views/proposals_list_view.dart';
import '../../uml/views/uml_diagram_view.dart';
import '../widgets/project_summary_tab.dart';

class ProjectDetailView extends StatefulWidget {
  final ProyectoModel proyecto;

  const ProjectDetailView({super.key, required this.proyecto});

  @override
  State<ProjectDetailView> createState() => _ProjectDetailViewState();
}

class _ProjectDetailViewState extends State<ProjectDetailView> {
  int _indiceActual = 0;

  @override
  void initState() {
    super.initState();
    ProposalMonitorService.instance.agregarProyecto(widget.proyecto.idProyecto);
  }

  final List<String> _titulos = [
    'Resumen',
    'Diagramas UML',
    'Código Generado',
    'Documentación',
    'Historial IA',
    'Propuestas',
  ];

  @override
  Widget build(BuildContext context) {
    final p = widget.proyecto;

    return Scaffold(
      appBar: AppBar(
        titleSpacing: 0,
        title: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(p.nombre, style: AppTypography.h3, overflow: TextOverflow.ellipsis),
            Text(
              _titulos[_indiceActual],
              style: AppTypography.bodySmall.copyWith(color: AppColors.primaryLight),
            ),
          ],
        ),
        actions: const [
          ReadOnlyBadge(compact: true),
          SizedBox(width: 16),
        ],
      ),
      body: Column(
        children: [
          const OfflineBanner(),
          Expanded(
            child: IndexedStack(
              index: _indiceActual,
              children: [
                // 5.1 Resumen
                ProjectSummaryTab(proyecto: p),

                // 5.2 Diagramas UML
                UmlDiagramView(idProyecto: p.idProyecto),

                // 5.3 Código Generado
                CodeBrowserView(idProyecto: p.idProyecto),

                // 5.4 Documentación y Requisitos
                DocumentationView(idProyecto: p.idProyecto),

                // 5.5 Historial de Conversaciones con IA
                IaHistoryView(idProyecto: p.idProyecto),

                // 5.6 Propuestas de Cambio
                ProposalsListView(idProyecto: p.idProyecto),
              ],
            ),
          ),
        ],
      ),
      bottomNavigationBar: NavigationBar(
        height: 66,
        selectedIndex: _indiceActual,
        onDestinationSelected: (idx) => setState(() => _indiceActual = idx),
        labelBehavior: NavigationDestinationLabelBehavior.alwaysShow,
        destinations: const [
          NavigationDestination(
            icon: Icon(Icons.dashboard_outlined, size: 20),
            selectedIcon: Icon(Icons.dashboard_rounded, size: 20),
            label: 'Resumen',
          ),
          NavigationDestination(
            icon: Icon(Icons.hub_outlined, size: 20),
            selectedIcon: Icon(Icons.hub_rounded, size: 20),
            label: 'UML',
          ),
          NavigationDestination(
            icon: Icon(Icons.code_outlined, size: 20),
            selectedIcon: Icon(Icons.code_rounded, size: 20),
            label: 'Código',
          ),
          NavigationDestination(
            icon: Icon(Icons.description_outlined, size: 20),
            selectedIcon: Icon(Icons.description_rounded, size: 20),
            label: 'Docs',
          ),
          NavigationDestination(
            icon: Icon(Icons.smart_toy_outlined, size: 20),
            selectedIcon: Icon(Icons.smart_toy_rounded, size: 20),
            label: 'IA Chat',
          ),
          NavigationDestination(
            icon: Icon(Icons.auto_awesome_outlined, size: 20),
            selectedIcon: Icon(Icons.auto_awesome_rounded, size: 20),
            label: 'Propuestas',
          ),
        ],
      ),
    );
  }
}

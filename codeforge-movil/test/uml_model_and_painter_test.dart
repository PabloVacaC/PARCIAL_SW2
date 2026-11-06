import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:codeforge_movil/modules/uml/models/uml_model.dart';
import 'package:codeforge_movil/modules/uml/widgets/uml_canvas_painter.dart';

void main() {
  group('UmlModel Backend JSON Deserialization Tests', () {
    test('Debe deserializar clases y relaciones desde el formato JSON del backend', () {
      final backendJson = {
        'idModelo': 1,
        'idProyecto': 1,
        'nombre': 'Diagrama de Clases Principal',
        'version': '1.127',
        'contenidoJson': {
          'elementos': [
            {
              'id': 'c1-uuid',
              'nombre': 'GRUPO',
              'tipo': 'CLASE',
              'posicion': {'x': 910.0, 'y': 600.0},
              'tamaño': {'ancho': 260.0, 'alto': 180.0},
              'atributos': [
                {
                  'id': 'a1',
                  'nombre': 'Id',
                  'tipo': 'Long',
                  'visibilidad': 'PUBLIC',
                },
                {
                  'id': 'a2',
                  'nombre': 'Nombre',
                  'tipo': 'String',
                  'visibilidad': 'PRIVATE',
                }
              ],
              'operaciones': [
                {
                  'id': 'o1',
                  'nombre': 'obtenerAlumnos',
                  'tipoRetorno': 'List<Alumno>',
                  'visibilidad': 'PUBLIC',
                  'parametros': ['filtro: String']
                }
              ]
            },
            {
              'id': 'c2-uuid',
              'nombre': 'DOCENTE',
              'tipo': 'CLASE',
              'posicion': {'x': 520.0, 'y': 190.0},
              'tamaño': {'ancho': 260.0, 'alto': 180.0},
              'atributos': [],
              'operaciones': []
            }
          ],
          'relaciones': [
            {
              'id': 'r1-uuid',
              'tipo': 'ASOCIACION',
              'origen': 'c1-uuid',
              'destino': 'c2-uuid',
              'rolOrigen': 'enseña',
              'rolDestino': 'tutor',
              'multiplicidadOrigen': '1..*',
              'multiplicidadDestino': '1'
            }
          ]
        }
      };

      final modelo = UmlDiagramaModel.fromBackendJson(backendJson);

      expect(modelo.idModelo, equals(1));
      expect(modelo.idProyecto, equals(1));
      expect(modelo.clases.length, equals(2));
      expect(modelo.relaciones.length, equals(1));

      // Verificación de Clase GRUPO
      final grupo = modelo.clases.firstWhere((c) => c.nombre == 'GRUPO');
      expect(grupo.id, equals('c1-uuid'));
      expect(grupo.x, equals(910.0));
      expect(grupo.y, equals(600.0));
      expect(grupo.width, equals(260.0));
      expect(grupo.height, equals(180.0));
      expect(grupo.atributos.length, equals(2));
      expect(grupo.operaciones.length, equals(1));

      // Verificación de la Relación y Cardinalidades
      final rel = modelo.relaciones.first;
      expect(rel.id, equals('r1-uuid'));
      expect(rel.origenId, equals('c1-uuid'));
      expect(rel.destinoId, equals('c2-uuid'));
      expect(rel.tipo, equals('ASOCIACION'));
      expect(rel.rolOrigen, equals('enseña'));
      expect(rel.rolDestino, equals('tutor'));
      expect(rel.multiplicidadOrigen, equals('1..*'));
      expect(rel.multiplicidadDestino, equals('1'));
    });

    test('Debe soportar relaciones que hacen referencia por nombre o UUID indistintamente', () {
      final backendJson = {
        'idModelo': 1,
        'contenidoJson': {
          'elementos': [
            {'id': 'uuid-alumno', 'nombre': 'Alumno', 'posicion': {'x': 100.0, 'y': 100.0}},
            {'id': 'uuid-materia', 'nombre': 'Materia', 'posicion': {'x': 400.0, 'y': 100.0}},
          ],
          'relaciones': [
            {
              'id': 'rel-nombre',
              'tipo': 'ASOCIACION',
              'origen': 'Alumno',
              'destino': 'Materia',
              'multiplicidadOrigen': '1',
              'multiplicidadDestino': '1..*'
            }
          ]
        }
      };

      final modelo = UmlDiagramaModel.fromBackendJson(backendJson);
      expect(modelo.relaciones.first.origenId, equals('Alumno'));
      expect(modelo.relaciones.first.destinoId, equals('Materia'));
      expect(modelo.relaciones.first.multiplicidadOrigen, equals('1'));
      expect(modelo.relaciones.first.multiplicidadDestino, equals('1..*'));
    });
  });

  group('UmlCanvasPainter Widget Render Tests', () {
    testWidgets('Debe dibujar el canvas con clases y relaciones sin fallos', (WidgetTester tester) async {
      final clases = [
        UmlClaseElement(
          id: 'c1',
          nombre: 'Docente',
          tipo: 'CLASE',
          x: 50,
          y: 50,
          width: 200,
          height: 120,
          atributos: [
            UmlAtributo(nombre: 'codigo', tipo: 'String', visibilidad: 'PRIVATE'),
          ],
          operaciones: [],
        ),
        UmlClaseElement(
          id: 'c2',
          nombre: 'Grupo',
          tipo: 'CLASE',
          x: 400,
          y: 200,
          width: 200,
          height: 120,
          atributos: [],
          operaciones: [],
        ),
      ];

      final relaciones = [
        UmlRelacionElement(
          id: 'r1',
          tipo: 'ASOCIACION',
          origenId: 'c1',
          destinoId: 'c2',
          rolOrigen: 'imparte',
          multiplicidadOrigen: '1',
          multiplicidadDestino: '1..*',
        ),
      ];

      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: CustomPaint(
              size: const Size(800, 600),
              painter: UmlCanvasPainter(
                clases: clases,
                relaciones: relaciones,
              ),
            ),
          ),
        ),
      );

      expect(find.byType(CustomPaint), findsWidgets);
    });
  });
}


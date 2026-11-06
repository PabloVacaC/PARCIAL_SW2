import 'package:flutter_test/flutter_test.dart';
import 'package:codeforge_movil/main.dart';

void main() {
  testWidgets('CodeForge Mobile smoke test', (WidgetTester tester) async {
    await tester.pumpWidget(const CodeForgeApp());
    expect(find.byType(CodeForgeApp), findsOneWidget);
  });
}

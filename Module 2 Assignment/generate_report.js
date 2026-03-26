const {
  Document,
  Packer,
  Paragraph,
  TextRun,
  HeadingLevel,
  AlignmentType,
  Table,
  TableRow,
  TableCell,
  BorderStyle,
  WidthType,
  ShadingType,
  PageBreak,
} = require('docx');
const fs = require('fs');
const path = require('path');

// ─── Helper builders ─────────────────────────────────────────────────────────

function heading1(text) {
  return new Paragraph({
    text,
    heading: HeadingLevel.HEADING_1,
    spacing: { before: 320, after: 120 },
  });
}

function heading2(text) {
  return new Paragraph({
    text,
    heading: HeadingLevel.HEADING_2,
    spacing: { before: 240, after: 80 },
  });
}

function body(text) {
  return new Paragraph({
    children: [new TextRun({ text, size: 24 })],
    spacing: { before: 80, after: 80 },
    alignment: AlignmentType.JUSTIFIED,
  });
}

function bullet(text) {
  return new Paragraph({
    children: [new TextRun({ text, size: 24 })],
    bullet: { level: 0 },
    spacing: { before: 60, after: 60 },
  });
}

function boldBody(label, rest) {
  return new Paragraph({
    children: [
      new TextRun({ text: label, bold: true, size: 24 }),
      new TextRun({ text: rest, size: 24 }),
    ],
    spacing: { before: 80, after: 80 },
  });
}

function buildComparisonTable(rows) {
  const headerShading = {
    fill: '1a73e8',
    type: ShadingType.SOLID,
    color: 'ffffff',
  };

  const headerCells = ['UI Element', 'Android Behavior', 'iOS Behavior'].map(
    (t) =>
      new TableCell({
        children: [
          new Paragraph({
            children: [new TextRun({ text: t, bold: true, color: 'FFFFFF', size: 22 })],
          }),
        ],
        shading: headerShading,
        width: { size: 33, type: WidthType.PERCENTAGE },
      })
  );

  const tableRows = [
    new TableRow({ children: headerCells, tableHeader: true }),
    ...rows.map(
      ([element, android, ios], i) =>
        new TableRow({
          children: [element, android, ios].map(
            (cellText, ci) =>
              new TableCell({
                children: [
                  new Paragraph({
                    children: [
                      new TextRun({
                        text: cellText,
                        bold: ci === 0,
                        size: 22,
                      }),
                    ],
                  }),
                ],
                shading:
                  i % 2 === 0
                    ? { fill: 'f0f4ff', type: ShadingType.SOLID }
                    : undefined,
                width: { size: 33, type: WidthType.PERCENTAGE },
              })
          ),
        })
    ),
  ];

  return new Table({
    rows: tableRows,
    width: { size: 100, type: WidthType.PERCENTAGE },
  });
}

// ─── Document content ─────────────────────────────────────────────────────────

const comparisonRows = [
  [
    'Navigation Bar',
    'Uses bottom Navigation Bar with icon + label; back navigation handled by hardware back button or back arrow in toolbar.',
    'Uses bottom Tab Bar with icon + label; uses swipe gesture or back-chevron in navigation header for back navigation.',
  ],
  [
    'Header / App Bar',
    'Material Design Toolbar; title left-aligned, flat appearance, often uses elevation/shadow.',
    'Navigation Bar; title center-aligned by default; supports large titles that collapse on scroll.',
  ],
  [
    'Typography',
    'Uses Roboto typeface; text sizes follow Material Design type scale (SP units).',
    'Uses San Francisco (SF Pro) typeface; Apple Human Interface Guidelines type scale.',
  ],
  [
    'Switch (Toggle)',
    'Animated pill toggle; thumb color changes on activation; uses Material ripple effect.',
    'iOS-style rounded toggle; green when active; no ripple; smoother spring animation.',
  ],
  [
    'Button Style',
    'Contained, outlined, or text buttons following Material Design 3; uses ripple on touch.',
    'Tappable text links or rounded-rect buttons; uses opacity feedback on press.',
  ],
  [
    'Dialog / Alert',
    'Material AlertDialog; title, message, buttons arranged at bottom with bold text; appears centered.',
    'UIAlertController; title capitalized, buttons stacked vertically or side-by-side; blurred background.',
  ],
  [
    'Status Bar',
    'Icons (time, battery, signal) left/right; darker by default; can be customized.',
    'Icons at top corners + Dynamic Island / notch area; automatically adapts to light/dark.',
  ],
  [
    'Shadow / Elevation',
    'Uses Material elevation system (dp); renders drop shadow on all sides.',
    'Uses shadow properties (shadowColor, shadowRadius, etc.); more natural diffuse shadow.',
  ],
  [
    'Touch Feedback',
    'Ripple effect radiates from touch point (TouchableNativeFeedback).',
    'Opacity reduces on touch (TouchableOpacity); no ripple effect.',
  ],
  [
    'Safe Area',
    'Needs padding for status bar only; typically 24 dp at top.',
    'Needs padding for notch, Dynamic Island, and home indicator; uses SafeAreaView.',
  ],
];

const doc = new Document({
  creator: 'Shane Potts',
  title: 'CSC2046 Module 2 – UI Platform Differences Report',
  description: 'Android vs iOS UI differences in the Student Profile app',
  styles: {
    default: {
      document: {
        run: { font: 'Calibri', size: 24, color: '1a1a2e' },
      },
      heading1: {
        run: { font: 'Calibri', size: 32, bold: true, color: '1a73e8' },
        paragraph: { spacing: { before: 400, after: 160 } },
      },
      heading2: {
        run: { font: 'Calibri', size: 26, bold: true, color: '1a1a2e' },
        paragraph: { spacing: { before: 280, after: 100 } },
      },
    },
  },
  sections: [
    {
      properties: {
        page: {
          margin: { top: 1440, bottom: 1440, left: 1080, right: 1080 },
        },
      },
      children: [
        // ── Title block ──────────────────────────────────────────────────────
        new Paragraph({
          children: [
            new TextRun({
              text: 'CSC2046 – Mobile App Development',
              bold: true,
              size: 28,
              color: '888888',
            }),
          ],
          alignment: AlignmentType.CENTER,
          spacing: { before: 0, after: 80 },
        }),
        new Paragraph({
          children: [
            new TextRun({
              text: 'Module 2 Assignment: Student Profile App',
              bold: true,
              size: 40,
              color: '1a73e8',
            }),
          ],
          alignment: AlignmentType.CENTER,
          spacing: { before: 0, after: 80 },
        }),
        new Paragraph({
          children: [
            new TextRun({
              text: 'UI Platform Differences Report – Android vs iOS',
              bold: true,
              size: 28,
              color: '333333',
            }),
          ],
          alignment: AlignmentType.CENTER,
          spacing: { before: 0, after: 80 },
        }),
        new Paragraph({
          children: [
            new TextRun({ text: 'Student: Shane Potts  |  Student ID: S02399685', size: 22, color: '666666' }),
          ],
          alignment: AlignmentType.CENTER,
          spacing: { before: 0, after: 60 },
        }),
        new Paragraph({
          children: [
            new TextRun({ text: 'Date: February 22, 2025  |  Instructor: Mason George', size: 22, color: '666666' }),
          ],
          alignment: AlignmentType.CENTER,
          spacing: { before: 0, after: 480 },
        }),

        // ── Section 1 ────────────────────────────────────────────────────────
        heading1('1. Introduction'),
        body(
          'This report examines the user interface (UI) differences between the Android and iOS versions of the Student Profile app developed for CSC2046 Module 2. Although the app was built with a single React Native / Expo codebase, each platform renders components differently based on its native design language. Understanding these differences is essential for building cross-platform mobile applications that feel native and intuitive on both devices.'
        ),
        body(
          'The Student Profile app consists of three screens: a Profile screen displaying student information, a Courses screen listing enrolled classes, and a Settings screen with preferences and account options. Navigation is implemented using React Navigation\'s bottom tab navigator combined with a native stack navigator per tab.'
        ),

        // ── Section 2 ────────────────────────────────────────────────────────
        heading1('2. Design Language Overview'),
        heading2('2.1 Android – Material Design 3'),
        body(
          'Android\'s UI follows Google\'s Material Design 3 (Material You) system. Key principles include bold color theming, dynamic color adaptation, ripple touch feedback, bottom navigation bars, and a flat card-based layout with elevation shadows that span all sides. Typography uses the Roboto typeface, and interactive elements rely on tactile feedback via ripple animations.'
        ),
        heading2('2.2 iOS – Apple Human Interface Guidelines (HIG)'),
        body(
          'iOS adheres to Apple\'s Human Interface Guidelines. The design emphasizes clarity, depth, and deference. Navigation relies on hierarchical stack-based patterns with prominent back-swipe gestures. Typography uses San Francisco (SF Pro). Buttons and interactable elements provide opacity-based feedback instead of ripples. The home indicator, Dynamic Island, and notch areas require specific safe-area handling not present on most Android devices.'
        ),

        // ── Section 3 ────────────────────────────────────────────────────────
        heading1('3. Screen-by-Screen Platform Differences'),
        heading2('3.1 Profile Screen'),
        body(
          'The Profile screen displays the student\'s hero banner, GPA card, bio, contact info, and achievements. On both platforms the layout is scroll-based with cards, but the rendering differs:'
        ),
        bullet('Header title alignment: Android places the navigation title left-aligned in the toolbar; iOS centers it and supports a collapsible large-title effect on scroll.'),
        bullet('Card shadows: iOS renders soft, diffuse drop shadows using shadowColor/shadowRadius properties; Android uses the elevation property which creates shadows on all four sides per Material spec.'),
        bullet('Avatar touch feedback: On Android the avatar area shows a Material ripple effect; on iOS pressing it reduces opacity smoothly.'),
        bullet('Font rendering: iOS uses SF Pro with slight optical size adjustments, giving text a slightly larger apparent size at the same point value compared to Android\'s Roboto.'),

        heading2('3.2 Courses Screen'),
        body(
          'The Courses screen uses a FlatList of expandable course cards. Differences observed include:'
        ),
        bullet('List scrolling: Android provides a native "overscroll glow" effect at list boundaries; iOS shows a rubber-band bounce effect.'),
        bullet('Expandable cards: The chevron icon and expand/collapse interaction behave identically, but the press highlight on Android shows a ripple while iOS shows an opacity change.'),
        bullet('FlatList performance: Both platforms use native scroll views, but Android may occasionally exhibit a brief flash on initial render (due to the JS bridge); this is rarely visible on modern devices.'),

        heading2('3.3 Settings Screen'),
        body(
          'The Settings screen highlights the most prominent platform differences due to its use of Switch toggles, Alert dialogs, and list separators.'
        ),
        bullet('Switch component: On iOS, the toggle is the classic rounded green/grey pill with a white thumb. On Android, Material Design renders a shorter pill with a colored thumb and background tint.'),
        bullet('Alert dialog: iOS Alert.alert() renders an iOS UIAlertController with title, message, and stacked or side-by-side buttons with a translucent blurred background. Android renders a centered Material AlertDialog.'),
        bullet('Row separators: iOS conventionally uses hairline-width (StyleSheet.hairlineWidth ≈ 0.5 px) separators; Android typically uses full 1 dp dividers.'),
        bullet('Status bar: The Settings top account banner uses a blue background. On Android the status bar icons remain white and visible over the blue; on iOS the status bar respects the light content style set via expo-status-bar.'),

        // ── Section 4 ────────────────────────────────────────────────────────
        heading1('4. Detailed Comparison Table'),
        body(
          'The table below summarizes the key UI differences across both platforms as observed in the Student Profile app:'
        ),
        new Paragraph({ spacing: { before: 120, after: 120 } }),
        buildComparisonTable(comparisonRows),
        new Paragraph({ spacing: { before: 240 } }),

        // ── Section 5 ────────────────────────────────────────────────────────
        heading1('5. Responsive Design Implementation'),
        body(
          'React Native\'s flexbox layout system is largely consistent across both platforms, making responsive design achievable from a single codebase. The following techniques were applied in the Student Profile app:'
        ),
        bullet('Dimensions API: Screen width was queried using Dimensions.get("window") to adapt grid layouts and card widths dynamically.'),
        bullet('Platform.select(): Used throughout the StyleSheet to supply platform-specific values — for example, applying iOS shadowColor/shadowRadius vs. Android elevation for card depth.'),
        bullet('SafeAreaView: Wrapped all screens in SafeAreaView from react-native-safe-area-context to correctly handle iPhone notch, Dynamic Island, and Android status bar padding.'),
        bullet('Percentage-based widths: Cards and summary bars use percentage widths rather than fixed pixel values, ensuring correct rendering on small phones, large phones, and tablets.'),
        bullet('headerLargeTitle (iOS only): The React Navigation stack was configured to enable large collapsible titles on iOS only, while Android retains the standard compact toolbar.'),

        // ── Section 6 ────────────────────────────────────────────────────────
        heading1('6. Conclusion'),
        body(
          'Building the Student Profile app revealed that while React Native provides excellent cross-platform consistency at the layout level, the two platforms diverge meaningfully in typography, shadow rendering, touch feedback, navigation patterns, and system component appearance (Switch, Alert). These differences stem from each platform\'s native design guidelines — Material Design 3 on Android and the Apple Human Interface Guidelines on iOS.'
        ),
        body(
          'Using Platform.select() and platform-aware component choices (such as headerLargeTitle on iOS) allows a single codebase to deliver a native-feeling experience on both platforms. Understanding and embracing these differences, rather than forcing a uniform appearance, results in apps that users find intuitive and polished on their respective devices.'
        ),

        // ── References ───────────────────────────────────────────────────────
        heading1('References'),
        bullet('Google LLC. (2023). Material Design 3 – Guidelines. https://m3.material.io/'),
        bullet('Apple Inc. (2024). Human Interface Guidelines. https://developer.apple.com/design/human-interface-guidelines/'),
        bullet('Expo. (2025). Expo Documentation. https://docs.expo.dev/'),
        bullet('React Navigation. (2025). React Navigation Documentation. https://reactnavigation.org/'),
        bullet('Meta. (2025). React Native Documentation. https://reactnative.dev/docs/'),
      ],
    },
  ],
});

// ─── Write file ───────────────────────────────────────────────────────────────

const outPath = path.join(
  __dirname,
  'CSC2046_Module2_UI_Platform_Report_ShanePotts.docx'
);

Packer.toBuffer(doc).then((buffer) => {
  fs.writeFileSync(outPath, buffer);
  console.log('Report written to:', outPath);
});

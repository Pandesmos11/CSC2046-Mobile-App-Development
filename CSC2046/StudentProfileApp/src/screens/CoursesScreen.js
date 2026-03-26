import React, { useState } from 'react';
import {
  View,
  Text,
  FlatList,
  TouchableOpacity,
  StyleSheet,
  Platform,
  Dimensions,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { SafeAreaView } from 'react-native-safe-area-context';
import { courses } from '../data/studentData';

const { width } = Dimensions.get('window');

const GRADE_COLORS = {
  A: '#2e7d32',
  'A-': '#388e3c',
  'B+': '#f57c00',
  B: '#f57c00',
  'B-': '#f57c00',
};

export default function CoursesScreen() {
  const [expandedId, setExpandedId] = useState(null);

  const totalCredits = courses.reduce((sum, c) => sum + c.credits, 0);

  const toggleExpand = (id) => {
    setExpandedId((prev) => (prev === id ? null : id));
  };

  return (
    <SafeAreaView style={styles.safeArea} edges={['bottom']}>
      {/* Summary bar */}
      <View style={styles.summaryBar}>
        <SummaryCell label="Courses" value={courses.length} />
        <View style={styles.divider} />
        <SummaryCell label="Credits" value={totalCredits} />
        <View style={styles.divider} />
        <SummaryCell label="Semester" value="SP 2025" />
      </View>

      <FlatList
        data={courses}
        keyExtractor={(item) => item.id}
        contentContainerStyle={styles.listContent}
        showsVerticalScrollIndicator={false}
        renderItem={({ item }) => (
          <CourseCard
            course={item}
            expanded={expandedId === item.id}
            onPress={() => toggleExpand(item.id)}
          />
        )}
      />
    </SafeAreaView>
  );
}

function SummaryCell({ label, value }) {
  return (
    <View style={styles.summaryCell}>
      <Text style={styles.summaryValue}>{value}</Text>
      <Text style={styles.summaryLabel}>{label}</Text>
    </View>
  );
}

function CourseCard({ course, expanded, onPress }) {
  const gradeColor = GRADE_COLORS[course.grade] || '#666';

  return (
    <TouchableOpacity
      style={styles.card}
      onPress={onPress}
      activeOpacity={0.8}
      accessibilityRole="button"
      accessibilityLabel={`${course.code} ${course.name}, Grade ${course.grade}`}
    >
      {/* Card Header */}
      <View style={styles.cardHeader}>
        <View style={styles.codeBlock}>
          <Text style={styles.courseCode}>{course.code}</Text>
        </View>
        <View style={styles.cardHeaderText}>
          <Text style={styles.courseName} numberOfLines={1}>
            {course.name}
          </Text>
          <Text style={styles.instructor}>{course.instructor}</Text>
        </View>
        <View style={styles.gradeChip}>
          <Text style={[styles.gradeText, { color: gradeColor }]}>
            {course.grade}
          </Text>
        </View>
        <Ionicons
          name={expanded ? 'chevron-up' : 'chevron-down'}
          size={18}
          color="#aaa"
          style={{ marginLeft: 6 }}
        />
      </View>

      {/* Expandable Detail */}
      {expanded && (
        <View style={styles.cardDetail}>
          <View style={styles.detailDivider} />
          <DetailRow icon="time-outline" text={course.schedule} />
          <DetailRow icon="location-outline" text={`Room ${course.room}`} />
          <DetailRow
            icon="school-outline"
            text={`${course.credits} credit${course.credits > 1 ? 's' : ''}`}
          />
        </View>
      )}
    </TouchableOpacity>
  );
}

function DetailRow({ icon, text }) {
  return (
    <View style={styles.detailRow}>
      <Ionicons name={icon} size={15} color="#1a73e8" />
      <Text style={styles.detailText}>{text}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    backgroundColor: '#f0f4ff',
  },

  // Summary bar
  summaryBar: {
    flexDirection: 'row',
    backgroundColor: '#1a73e8',
    paddingVertical: 16,
    paddingHorizontal: 24,
    justifyContent: 'space-around',
    alignItems: 'center',
  },
  summaryCell: {
    alignItems: 'center',
    flex: 1,
  },
  summaryValue: {
    fontSize: 22,
    fontWeight: '800',
    color: '#fff',
  },
  summaryLabel: {
    fontSize: 11,
    color: 'rgba(255,255,255,0.75)',
    textTransform: 'uppercase',
    letterSpacing: 0.8,
    marginTop: 2,
  },
  divider: {
    width: 1,
    height: 36,
    backgroundColor: 'rgba(255,255,255,0.3)',
  },

  // List
  listContent: {
    padding: 16,
    gap: 12,
    paddingBottom: 28,
  },

  // Cards
  card: {
    backgroundColor: '#fff',
    borderRadius: 16,
    padding: 16,
    ...Platform.select({
      ios: {
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 2 },
        shadowOpacity: 0.07,
        shadowRadius: 8,
      },
      android: {
        elevation: 2,
      },
    }),
  },
  cardHeader: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  codeBlock: {
    backgroundColor: '#e8f0fe',
    borderRadius: 8,
    paddingHorizontal: 8,
    paddingVertical: 6,
    marginRight: 12,
    minWidth: 72,
    alignItems: 'center',
  },
  courseCode: {
    fontSize: 12,
    fontWeight: '700',
    color: '#1a73e8',
    letterSpacing: 0.5,
  },
  cardHeaderText: {
    flex: 1,
  },
  courseName: {
    fontSize: 15,
    fontWeight: '600',
    color: '#1a1a2e',
  },
  instructor: {
    fontSize: 12,
    color: '#888',
    marginTop: 2,
  },
  gradeChip: {
    backgroundColor: '#f5f5f5',
    borderRadius: 8,
    paddingHorizontal: 10,
    paddingVertical: 5,
    marginLeft: 8,
  },
  gradeText: {
    fontSize: 15,
    fontWeight: '800',
  },

  // Expandable detail
  cardDetail: {
    marginTop: 8,
  },
  detailDivider: {
    height: 1,
    backgroundColor: '#f0f0f0',
    marginBottom: 10,
  },
  detailRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
    marginBottom: 6,
  },
  detailText: {
    fontSize: 13,
    color: '#555',
  },
});

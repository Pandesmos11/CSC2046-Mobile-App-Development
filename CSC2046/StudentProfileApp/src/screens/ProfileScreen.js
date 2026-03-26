import React from 'react';
import {
  View,
  Text,
  ScrollView,
  StyleSheet,
  Platform,
  Dimensions,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { SafeAreaView } from 'react-native-safe-area-context';
import { student, achievements } from '../data/studentData';

const { width } = Dimensions.get('window');

export default function ProfileScreen() {
  return (
    <SafeAreaView style={styles.safeArea} edges={['top', 'bottom']}>
      <ScrollView
        contentContainerStyle={styles.scrollContent}
        showsVerticalScrollIndicator={false}
      >
        {/* Hero / Avatar Section */}
        <View style={styles.heroSection}>
          <View style={styles.avatarContainer}>
            <Ionicons name="person" size={64} color="#fff" />
          </View>
          <Text style={styles.name}>{student.name}</Text>
          <Text style={styles.studentId}>ID: {student.studentId}</Text>
          <View style={styles.badgeRow}>
            <View style={styles.badge}>
              <Text style={styles.badgeText}>{student.major}</Text>
            </View>
            <View style={[styles.badge, styles.badgeSecondary]}>
              <Text style={styles.badgeText}>{student.year}</Text>
            </View>
          </View>
        </View>

        {/* GPA Card */}
        <View style={styles.gpaCard}>
          <Text style={styles.gpaValue}>{student.gpa}</Text>
          <Text style={styles.gpaLabel}>Cumulative GPA</Text>
        </View>

        {/* Bio Section */}
        <View style={styles.card}>
          <Text style={styles.cardTitle}>About</Text>
          <Text style={styles.bioText}>{student.bio}</Text>
        </View>

        {/* Contact Info */}
        <View style={styles.card}>
          <Text style={styles.cardTitle}>Contact Information</Text>
          <InfoRow icon="mail-outline" label="Email" value={student.email} />
          <InfoRow icon="person-outline" label="Advisor" value={student.advisor} />
        </View>

        {/* Achievements */}
        <View style={styles.card}>
          <Text style={styles.cardTitle}>Achievements</Text>
          {achievements.map((a) => (
            <View key={a.id} style={styles.achievementRow}>
              <Ionicons name="ribbon-outline" size={20} color="#1a73e8" />
              <Text style={styles.achievementText}>{a.title}</Text>
            </View>
          ))}
        </View>
      </ScrollView>
    </SafeAreaView>
  );
}

function InfoRow({ icon, label, value }) {
  return (
    <View style={styles.infoRow}>
      <Ionicons name={icon} size={18} color="#666" style={styles.infoIcon} />
      <View style={styles.infoTextGroup}>
        <Text style={styles.infoLabel}>{label}</Text>
        <Text style={styles.infoValue}>{value}</Text>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    backgroundColor: '#f0f4ff',
  },
  scrollContent: {
    paddingBottom: 24,
  },

  // Hero
  heroSection: {
    backgroundColor: '#1a73e8',
    alignItems: 'center',
    paddingTop: 32,
    paddingBottom: 28,
    paddingHorizontal: 16,
  },
  avatarContainer: {
    width: 108,
    height: 108,
    borderRadius: 54,
    backgroundColor: 'rgba(255,255,255,0.25)',
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 12,
    borderWidth: 3,
    borderColor: '#fff',
  },
  name: {
    fontSize: Platform.select({ ios: 26, android: 24 }),
    fontWeight: '700',
    color: '#fff',
    marginBottom: 4,
    textAlign: 'center',
  },
  studentId: {
    fontSize: 13,
    color: 'rgba(255,255,255,0.8)',
    marginBottom: 12,
    letterSpacing: 0.5,
  },
  badgeRow: {
    flexDirection: 'row',
    gap: 8,
  },
  badge: {
    backgroundColor: 'rgba(255,255,255,0.2)',
    paddingHorizontal: 12,
    paddingVertical: 5,
    borderRadius: 20,
    borderWidth: 1,
    borderColor: 'rgba(255,255,255,0.4)',
  },
  badgeSecondary: {
    backgroundColor: 'rgba(255,255,255,0.1)',
  },
  badgeText: {
    color: '#fff',
    fontSize: 13,
    fontWeight: '600',
  },

  // GPA Card
  gpaCard: {
    backgroundColor: '#fff',
    marginHorizontal: 16,
    marginTop: -16,
    borderRadius: 16,
    paddingVertical: 20,
    alignItems: 'center',
    ...Platform.select({
      ios: {
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 4 },
        shadowOpacity: 0.08,
        shadowRadius: 12,
      },
      android: {
        elevation: 4,
      },
    }),
  },
  gpaValue: {
    fontSize: 40,
    fontWeight: '800',
    color: '#1a73e8',
  },
  gpaLabel: {
    fontSize: 13,
    color: '#888',
    marginTop: 2,
    textTransform: 'uppercase',
    letterSpacing: 1,
  },

  // Cards
  card: {
    backgroundColor: '#fff',
    marginHorizontal: 16,
    marginTop: 16,
    borderRadius: 16,
    padding: 20,
    ...Platform.select({
      ios: {
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 2 },
        shadowOpacity: 0.06,
        shadowRadius: 8,
      },
      android: {
        elevation: 2,
      },
    }),
  },
  cardTitle: {
    fontSize: 16,
    fontWeight: '700',
    color: '#1a1a2e',
    marginBottom: 14,
    letterSpacing: 0.3,
  },
  bioText: {
    fontSize: 14,
    color: '#555',
    lineHeight: 22,
  },

  // Info rows
  infoRow: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    marginBottom: 12,
  },
  infoIcon: {
    marginTop: 2,
    marginRight: 12,
  },
  infoTextGroup: {
    flex: 1,
  },
  infoLabel: {
    fontSize: 11,
    color: '#999',
    textTransform: 'uppercase',
    letterSpacing: 0.8,
  },
  infoValue: {
    fontSize: 14,
    color: '#333',
    marginTop: 1,
  },

  // Achievements
  achievementRow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 10,
    gap: 10,
  },
  achievementText: {
    fontSize: 14,
    color: '#333',
  },
});

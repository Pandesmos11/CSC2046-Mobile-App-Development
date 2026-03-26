import React, { useState } from 'react';
import {
  View,
  Text,
  ScrollView,
  Switch,
  TouchableOpacity,
  StyleSheet,
  Platform,
  Alert,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { SafeAreaView } from 'react-native-safe-area-context';
import { student } from '../data/studentData';

export default function SettingsScreen() {
  const [notifications, setNotifications] = useState(true);
  const [darkMode, setDarkMode] = useState(false);
  const [emailUpdates, setEmailUpdates] = useState(true);

  const handleLogout = () => {
    Alert.alert('Log Out', 'Are you sure you want to log out?', [
      { text: 'Cancel', style: 'cancel' },
      { text: 'Log Out', style: 'destructive', onPress: () => {} },
    ]);
  };

  return (
    <SafeAreaView style={styles.safeArea} edges={['bottom']}>
      <ScrollView
        contentContainerStyle={styles.scrollContent}
        showsVerticalScrollIndicator={false}
      >
        {/* Account Card */}
        <View style={styles.accountCard}>
          <View style={styles.accountAvatar}>
            <Ionicons name="person" size={32} color="#fff" />
          </View>
          <View style={styles.accountInfo}>
            <Text style={styles.accountName}>{student.name}</Text>
            <Text style={styles.accountEmail}>{student.email}</Text>
          </View>
          <TouchableOpacity style={styles.editButton}>
            <Text style={styles.editButtonText}>Edit</Text>
          </TouchableOpacity>
        </View>

        {/* Notifications Section */}
        <SectionHeader title="Notifications" />
        <View style={styles.settingsGroup}>
          <ToggleRow
            icon="notifications-outline"
            label="Push Notifications"
            description="Alerts for grades and announcements"
            value={notifications}
            onValueChange={setNotifications}
          />
          <Separator />
          <ToggleRow
            icon="mail-outline"
            label="Email Updates"
            description="Weekly progress reports"
            value={emailUpdates}
            onValueChange={setEmailUpdates}
          />
        </View>

        {/* Display Section */}
        <SectionHeader title="Display" />
        <View style={styles.settingsGroup}>
          <ToggleRow
            icon="moon-outline"
            label="Dark Mode"
            description="Switch to dark color scheme"
            value={darkMode}
            onValueChange={setDarkMode}
          />
        </View>

        {/* Academic Section */}
        <SectionHeader title="Academic" />
        <View style={styles.settingsGroup}>
          <ActionRow
            icon="document-text-outline"
            label="Download Transcript"
            onPress={() =>
              Alert.alert('Download', 'Transcript download would begin here.')
            }
          />
          <Separator />
          <ActionRow
            icon="calendar-outline"
            label="Academic Calendar"
            onPress={() =>
              Alert.alert('Calendar', 'Academic calendar would open here.')
            }
          />
          <Separator />
          <ActionRow
            icon="help-circle-outline"
            label="Contact Advisor"
            onPress={() =>
              Alert.alert('Advisor', `Your advisor is ${student.advisor}.`)
            }
          />
        </View>

        {/* App Info */}
        <SectionHeader title="About" />
        <View style={styles.settingsGroup}>
          <InfoRow label="App Version" value="1.0.0" />
          <Separator />
          <InfoRow label="Platform" value={Platform.OS === 'ios' ? 'iOS' : 'Android'} />
          <Separator />
          <InfoRow label="Institution" value="FRCC" />
        </View>

        {/* Logout */}
        <TouchableOpacity style={styles.logoutButton} onPress={handleLogout}>
          <Ionicons name="log-out-outline" size={20} color="#d32f2f" />
          <Text style={styles.logoutText}>Log Out</Text>
        </TouchableOpacity>

        <Text style={styles.footerNote}>
          Student Profile App · CSC2046 · Spring 2025
        </Text>
      </ScrollView>
    </SafeAreaView>
  );
}

// ─── Sub-components ────────────────────────────────────────────────────────────

function SectionHeader({ title }) {
  return (
    <Text style={styles.sectionHeader}>{title.toUpperCase()}</Text>
  );
}

function Separator() {
  return <View style={styles.separator} />;
}

function ToggleRow({ icon, label, description, value, onValueChange }) {
  return (
    <View style={styles.row}>
      <View style={styles.rowIconWrap}>
        <Ionicons name={icon} size={20} color="#1a73e8" />
      </View>
      <View style={styles.rowText}>
        <Text style={styles.rowLabel}>{label}</Text>
        {description ? (
          <Text style={styles.rowDescription}>{description}</Text>
        ) : null}
      </View>
      <Switch
        value={value}
        onValueChange={onValueChange}
        trackColor={{ false: '#ccc', true: '#a8c5f7' }}
        thumbColor={Platform.select({
          ios: '#fff',
          android: value ? '#1a73e8' : '#f4f3f4',
        })}
      />
    </View>
  );
}

function ActionRow({ icon, label, onPress }) {
  return (
    <TouchableOpacity style={styles.row} onPress={onPress} activeOpacity={0.7}>
      <View style={styles.rowIconWrap}>
        <Ionicons name={icon} size={20} color="#1a73e8" />
      </View>
      <Text style={[styles.rowLabel, { flex: 1 }]}>{label}</Text>
      <Ionicons name="chevron-forward" size={18} color="#ccc" />
    </TouchableOpacity>
  );
}

function InfoRow({ label, value }) {
  return (
    <View style={styles.row}>
      <Text style={[styles.rowLabel, { flex: 1 }]}>{label}</Text>
      <Text style={styles.rowValue}>{value}</Text>
    </View>
  );
}

// ─── Styles ────────────────────────────────────────────────────────────────────

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    backgroundColor: '#f0f4ff',
  },
  scrollContent: {
    paddingBottom: 32,
  },

  // Account card
  accountCard: {
    backgroundColor: '#1a73e8',
    flexDirection: 'row',
    alignItems: 'center',
    padding: 20,
    marginBottom: 8,
  },
  accountAvatar: {
    width: 60,
    height: 60,
    borderRadius: 30,
    backgroundColor: 'rgba(255,255,255,0.25)',
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 14,
  },
  accountInfo: {
    flex: 1,
  },
  accountName: {
    fontSize: 17,
    fontWeight: '700',
    color: '#fff',
  },
  accountEmail: {
    fontSize: 13,
    color: 'rgba(255,255,255,0.8)',
    marginTop: 2,
  },
  editButton: {
    backgroundColor: 'rgba(255,255,255,0.2)',
    paddingHorizontal: 14,
    paddingVertical: 7,
    borderRadius: 20,
    borderWidth: 1,
    borderColor: 'rgba(255,255,255,0.4)',
  },
  editButtonText: {
    color: '#fff',
    fontSize: 13,
    fontWeight: '600',
  },

  // Section headers
  sectionHeader: {
    fontSize: 11,
    fontWeight: '700',
    color: '#888',
    letterSpacing: 1,
    marginTop: 20,
    marginBottom: 6,
    marginHorizontal: 16,
  },

  // Settings groups
  settingsGroup: {
    backgroundColor: '#fff',
    marginHorizontal: 16,
    borderRadius: 16,
    overflow: 'hidden',
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
  separator: {
    height: StyleSheet.hairlineWidth,
    backgroundColor: '#f0f0f0',
    marginLeft: 56,
  },

  // Rows
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 16,
    paddingVertical: 14,
  },
  rowIconWrap: {
    width: 32,
    marginRight: 12,
    alignItems: 'center',
  },
  rowText: {
    flex: 1,
  },
  rowLabel: {
    fontSize: 15,
    color: '#1a1a2e',
  },
  rowDescription: {
    fontSize: 12,
    color: '#999',
    marginTop: 1,
  },
  rowValue: {
    fontSize: 14,
    color: '#888',
  },

  // Logout
  logoutButton: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 8,
    marginHorizontal: 16,
    marginTop: 24,
    paddingVertical: 14,
    backgroundColor: '#fff',
    borderRadius: 16,
    borderWidth: 1,
    borderColor: '#ffcdd2',
  },
  logoutText: {
    fontSize: 15,
    fontWeight: '600',
    color: '#d32f2f',
  },

  // Footer
  footerNote: {
    textAlign: 'center',
    fontSize: 11,
    color: '#bbb',
    marginTop: 24,
    letterSpacing: 0.5,
  },
});

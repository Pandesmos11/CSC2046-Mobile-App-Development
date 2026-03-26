// ============================================================
// Name:        Shane Potts
// Date:        03/11/2026
// Course:      CSC2046 — Mobile App Development
// Module:      4 — Personal Notes (iOS)
// File:        NotesListViewController.swift
// Description: Root screen — displays all saved notes in a
//              UITableView sorted by most-recently-updated.
//              Navigation bar "+" button opens a blank editor.
//              Tapping a row opens that note for editing.
//              Swipe-left on any row reveals a "Delete" action.
//              viewWillAppear reloads the list so any change
//              made in the editor is immediately visible.
// Inputs:      User taps (+ button, row tap, swipe delete)
// Outputs:     UserDefaults changes via NotesRepository
// ============================================================

import UIKit

class NotesListViewController: UITableViewController {

    // ── Data ────────────────────────────────────────────────────
    private var notes: [Note] = []
    private let repo = NotesRepository.shared

    // ── Date formatter shared across all cells ─────────────────
    private let dateFormatter: DateFormatter = {
        let f = DateFormatter()
        f.dateStyle = .medium
        f.timeStyle = .short
        return f
    }()

    // ==========================================================
    // viewDidLoad — register cell class and navigation items
    // ==========================================================
    override func viewDidLoad() {
        super.viewDidLoad()

        title = "Personal Notes"

        // Navigation bar appearance
        let appearance = UINavigationBarAppearance()
        appearance.configureWithOpaqueBackground()
        appearance.backgroundColor = UIColor(named: "AccentColor") ?? .systemBlue
        appearance.titleTextAttributes = [.foregroundColor: UIColor.white]
        navigationController?.navigationBar.standardAppearance   = appearance
        navigationController?.navigationBar.scrollEdgeAppearance = appearance
        navigationController?.navigationBar.tintColor            = .white

        // "+" button in the top-right corner
        navigationItem.rightBarButtonItem = UIBarButtonItem(
            barButtonSystemItem: .add,
            target: self,
            action: #selector(addNoteTapped)
        )

        // "About" button in the top-left corner
        navigationItem.leftBarButtonItem = UIBarButtonItem(
            title: "About",
            style: .plain,
            target: self,
            action: #selector(showAbout)
        )

        tableView.register(NoteCell.self, forCellReuseIdentifier: NoteCell.reuseId)
        tableView.rowHeight          = UITableView.automaticDimension
        tableView.estimatedRowHeight = 80
        tableView.separatorInset     = UIEdgeInsets(top: 0, left: 16, bottom: 0, right: 0)
    }

    // ==========================================================
    // viewWillAppear — reload data every time screen becomes visible
    //   This covers: returning from editor after save/delete,
    //   and the initial load when the app first launches.
    // ==========================================================
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        notes = repo.loadNotes()
        tableView.reloadData()
        updateEmptyState()
    }

    // ==========================================================
    // addNoteTapped — open a blank editor for a new note
    // ==========================================================
    @objc private func addNoteTapped() {
        let editor = NoteEditorViewController()
        editor.mode = .new
        navigationController?.pushViewController(editor, animated: true)
    }

    // ==========================================================
    // showAbout — display app info
    // ==========================================================
    @objc private func showAbout() {
        let alert = UIAlertController(
            title:   "About",
            message: "Personal Notes  v1.0\n\nDeveloper:  Shane Potts\nCourse:     CSC2046\nSchool:     Front Range Community College\nTerm:       Spring 2026\n\nPersistence: UserDefaults + Codable",
            preferredStyle: .alert
        )
        alert.addAction(UIAlertAction(title: "OK", style: .default))
        present(alert, animated: true)
    }

    // ==========================================================
    // UITableViewDataSource
    // ==========================================================
    override func tableView(_ tv: UITableView,
                            numberOfRowsInSection section: Int) -> Int {
        return notes.count
    }

    override func tableView(_ tv: UITableView,
                            cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tv.dequeueReusableCell(
            withIdentifier: NoteCell.reuseId,
            for: indexPath
        ) as! NoteCell
        cell.configure(with: notes[indexPath.row], formatter: dateFormatter)
        return cell
    }

    // ==========================================================
    // UITableViewDelegate — tap to edit
    // ==========================================================
    override func tableView(_ tv: UITableView,
                            didSelectRowAt indexPath: IndexPath) {
        tv.deselectRow(at: indexPath, animated: true)
        let editor = NoteEditorViewController()
        editor.mode = .edit(notes[indexPath.row])
        navigationController?.pushViewController(editor, animated: true)
    }

    // ==========================================================
    // Swipe-to-delete — left swipe reveals native Delete action
    // ==========================================================
    override func tableView(_ tv: UITableView,
                            commit editingStyle: UITableViewCell.EditingStyle,
                            forRowAt indexPath: IndexPath) {
        guard editingStyle == .delete else { return }
        let noteId = notes[indexPath.row].id
        notes.remove(at: indexPath.row)
        repo.deleteNote(id: noteId)
        tv.deleteRows(at: [indexPath], with: .fade)
        updateEmptyState()
    }

    // ==========================================================
    // updateEmptyState — show/hide the placeholder label
    // ==========================================================
    private func updateEmptyState() {
        if notes.isEmpty {
            let label = UILabel()
            label.text          = "No notes yet.\nTap + to create one."
            label.textAlignment = .center
            label.numberOfLines = 2
            label.textColor     = .secondaryLabel
            label.font          = UIFont.systemFont(ofSize: 17)
            tableView.backgroundView = label
        } else {
            tableView.backgroundView = nil
        }
    }
}

// ============================================================
// NoteCell — custom UITableViewCell showing title / preview / date
// ============================================================
class NoteCell: UITableViewCell {

    static let reuseId = "NoteCell"

    private let titleLabel   = UILabel()
    private let previewLabel = UILabel()
    private let dateLabel    = UILabel()

    // ==========================================================
    // init — programmatic layout using UIStackView
    // ==========================================================
    override init(style: UITableViewCell.CellStyle,
                  reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        setupViews()
    }

    required init?(coder: NSCoder) { fatalError("Not using Storyboards") }

    private func setupViews() {
        // Title
        titleLabel.font         = UIFont.boldSystemFont(ofSize: 16)
        titleLabel.numberOfLines = 1

        // Body preview
        previewLabel.font         = UIFont.systemFont(ofSize: 13)
        previewLabel.textColor    = .secondaryLabel
        previewLabel.numberOfLines = 2

        // Timestamp (right-aligned)
        dateLabel.font          = UIFont.systemFont(ofSize: 11)
        dateLabel.textColor     = .tertiaryLabel
        dateLabel.textAlignment = .right

        let stack = UIStackView(arrangedSubviews: [titleLabel, previewLabel, dateLabel])
        stack.axis    = .vertical
        stack.spacing = 4
        stack.translatesAutoresizingMaskIntoConstraints = false

        contentView.addSubview(stack)
        NSLayoutConstraint.activate([
            stack.topAnchor.constraint(equalTo: contentView.topAnchor, constant: 12),
            stack.leadingAnchor.constraint(equalTo: contentView.leadingAnchor, constant: 16),
            stack.trailingAnchor.constraint(equalTo: contentView.trailingAnchor, constant: -16),
            stack.bottomAnchor.constraint(equalTo: contentView.bottomAnchor, constant: -12)
        ])

        accessoryType = .disclosureIndicator
    }

    // ==========================================================
    // configure — populate labels from a Note model
    // ==========================================================
    func configure(with note: Note, formatter: DateFormatter) {
        titleLabel.text   = note.title.isEmpty ? "Untitled" : note.title
        // Collapse newlines so the two-line preview reads cleanly
        previewLabel.text = String(note.body.prefix(100))
            .replacingOccurrences(of: "\n", with: " ")
        dateLabel.text    = formatter.string(from: note.updatedAt)
    }
}

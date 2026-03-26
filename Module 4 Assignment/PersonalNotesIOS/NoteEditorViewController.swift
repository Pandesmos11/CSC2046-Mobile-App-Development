// ============================================================
// Name:        Shane Potts
// Date:        03/11/2026
// Course:      CSC2046 — Mobile App Development
// Module:      4 — Personal Notes (iOS)
// File:        NoteEditorViewController.swift
// Description: Create / edit a single note.
//              Mode enum controls whether the editor opens blank
//              (new) or pre-filled (edit).
//
//              Navigation bar items:
//                .new  → "Save" right, no left button
//                .edit → "Save" right, "Delete" left (red)
//
//              Saving:
//                • "Save" tap → explicit save, pop back
//                • Back chevron (navigation back) → auto-save if
//                  title or body has content, otherwise just pop
//
//              The iOS auto-save approach mirrors the Android
//              NoteEditorActivity's OnBackPressedCallback pattern.
// Inputs:      Note (optional), title UITextField, body UITextView
// Outputs:     UserDefaults changes via NotesRepository
// ============================================================

import UIKit

class NoteEditorViewController: UIViewController {

    // ── Mode ────────────────────────────────────────────────────
    enum Mode {
        case new
        case edit(Note)
    }
    var mode: Mode = .new

    // ── Data ────────────────────────────────────────────────────
    private let repo = NotesRepository.shared
    private var currentNote: Note?

    // ── Views ───────────────────────────────────────────────────
    private let titleField = UITextField()
    private let bodyView   = UITextView()

    // ── Flags ───────────────────────────────────────────────────
    // Prevents double-saves when both viewWillDisappear and the
    // explicit Save button are tapped in quick succession.
    private var hasSaved = false

    // ==========================================================
    // viewDidLoad — layout + load existing content
    // ==========================================================
    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = .systemBackground
        setupNavBar()
        setupLayout()
        loadContent()
    }

    // ==========================================================
    // viewDidAppear — auto-open keyboard on new notes
    // ==========================================================
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        if case .new = mode { titleField.becomeFirstResponder() }
    }

    // ==========================================================
    // viewWillDisappear — auto-save when navigating back via the
    //   system back chevron (iOS equivalent of OnBackPressedCallback)
    // ==========================================================
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        // isMovingFromParent is true when popped (back navigation),
        // not when another view is pushed on top.
        if isMovingFromParent && !hasSaved {
            performSave()
        }
    }

    // ==========================================================
    // setupNavBar — configure navigation bar title and buttons
    // ==========================================================
    private func setupNavBar() {
        navigationItem.rightBarButtonItem = UIBarButtonItem(
            title: "Save",
            style: .done,
            target: self,
            action: #selector(saveTapped)
        )

        if case .edit(_) = mode {
            let deleteBtn = UIBarButtonItem(
                title: "Delete",
                style: .plain,
                target: self,
                action: #selector(deleteTapped)
            )
            deleteBtn.tintColor = .systemRed
            navigationItem.leftBarButtonItem = deleteBtn
        }
    }

    // ==========================================================
    // setupLayout — programmatic UIStackView layout
    //   title field → thin divider → full-height body text view
    //   The body view's bottom is pinned to keyboardLayoutGuide
    //   so it resizes correctly when the keyboard appears.
    // ==========================================================
    private func setupLayout() {
        titleField.placeholder  = "Title"
        titleField.font         = UIFont.boldSystemFont(ofSize: 22)
        titleField.borderStyle  = .none
        titleField.returnKeyType = .next
        titleField.delegate     = self

        bodyView.font                            = UIFont.systemFont(ofSize: 16)
        bodyView.textContainerInset              = .zero
        bodyView.textContainer.lineFragmentPadding = 0
        bodyView.backgroundColor                  = .clear

        let divider = UIView()
        divider.backgroundColor = .separator

        let stack = UIStackView(arrangedSubviews: [titleField, divider, bodyView])
        stack.axis    = .vertical
        stack.spacing = 12
        stack.translatesAutoresizingMaskIntoConstraints = false

        view.addSubview(stack)

        NSLayoutConstraint.activate([
            divider.heightAnchor.constraint(equalToConstant: 0.5),

            stack.topAnchor.constraint(
                equalTo: view.safeAreaLayoutGuide.topAnchor, constant: 16),
            stack.leadingAnchor.constraint(
                equalTo: view.leadingAnchor, constant: 16),
            stack.trailingAnchor.constraint(
                equalTo: view.trailingAnchor, constant: -16),
            stack.bottomAnchor.constraint(
                equalTo: view.keyboardLayoutGuide.topAnchor, constant: -8)
        ])
    }

    // ==========================================================
    // loadContent — populate fields for edit mode
    // ==========================================================
    private func loadContent() {
        if case .edit(let note) = mode {
            title         = "Edit Note"
            titleField.text = note.title
            bodyView.text   = note.body
            currentNote     = note
        } else {
            title = "New Note"
        }
    }

    // ==========================================================
    // performSave — write fields to UserDefaults via the repo.
    //   Empty title AND empty body → nothing saved, just return.
    // ==========================================================
    @discardableResult
    private func performSave() -> Bool {
        let titleText = (titleField.text ?? "").trimmingCharacters(in: .whitespaces)
        let bodyText  = (bodyView.text  ?? "").trimmingCharacters(in: .whitespaces)

        if titleText.isEmpty && bodyText.isEmpty { return false }

        hasSaved = true

        if case .edit(_) = mode, var note = currentNote {
            note.title     = titleText
            note.body      = bodyText
            note.updatedAt = Date()
            repo.updateNote(note)
        } else {
            repo.addNote(Note(title: titleText, body: bodyText))
        }
        return true
    }

    // ==========================================================
    // saveTapped — explicit Save button; save then pop
    // ==========================================================
    @objc private func saveTapped() {
        performSave()
        hasSaved = true   // prevent double-save in viewWillDisappear
        navigationController?.popViewController(animated: true)
    }

    // ==========================================================
    // deleteTapped — confirm then delete and pop
    // ==========================================================
    @objc private func deleteTapped() {
        let alert = UIAlertController(
            title:          "Delete Note",
            message:        "Delete this note? This cannot be undone.",
            preferredStyle: .alert
        )
        alert.addAction(
            UIAlertAction(title: "Delete", style: .destructive) { [weak self] _ in
                if let id = self?.currentNote?.id {
                    self?.repo.deleteNote(id: id)
                }
                self?.hasSaved = true   // nothing left to save
                self?.navigationController?.popViewController(animated: true)
            }
        )
        alert.addAction(UIAlertAction(title: "Cancel", style: .cancel))
        present(alert, animated: true)
    }
}

// ============================================================
// UITextFieldDelegate — Tab / Return in title moves focus to body
// ============================================================
extension NoteEditorViewController: UITextFieldDelegate {
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        bodyView.becomeFirstResponder()
        return false
    }
}

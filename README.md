KNOWN BUGS

- ComboBoxes are buggy and don't always select the correct pokemon after it is added/edited. Caused by updating the
  FilteredList selection to null every time the contents of the FilteredList changes (an add or edit of a pokemon),
  which causes the window to be set to the current selected pokemon. IN PROGRESS
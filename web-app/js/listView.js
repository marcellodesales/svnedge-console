/*
 * CollabNet Subversion Edge
 * Copyright (C) 2011, CollabNet Inc. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * This file provides js functions for "list view" item selection and actions
 */

String.prototype.trim = function () {
    return this.replace(/^\s*/, "").replace(/\s*$/, "");
}

Event.observe(window, 'load', function() {
    // add observer to checkboxes for enabling / disabling checkboxes
    var allItemSelectCheckboxes = $$('input.listViewSelectItem');
    allItemSelectCheckboxes.each(function(item) {
        Event.observe(item, 'click', updateActionButtons);
    })

    // add observer to action buttons with confirmation messages
    $$('input.listViewAction').each (function(s) {
        var confirmMessageElement = s.readAttribute('confirmMessageElement')
        if (confirmMessageElement) {
            Event.observe(s, 'click', function(e){
                // stop this button click from submitting form
                Event.stop(e)
                // confirm dialog, with callback functions for "ok" and "cancel"
                confirmAction($(confirmMessageElement).innerHTML,
                        function() {
                            // on "ok", submit the form
                            // if "type this" confirmation present, verify user input matches first
                            if (s.readAttribute('confirmTypeThis')) {
                                var typeThis = s.readAttribute('confirmTypeThis')
                                var userInput = $('_confirmTypeThisInput').value;
                                if (typeThis != userInput) {
                                    new Effect.Shake(Windows.focusedWindow.getId());
                                    return;
                                }
                            }
                            // submit the form with the original button properties transferred to a hidden field
                            var action = new Element('input', { type: 'hidden',  name: s.readAttribute('name'), value: s.readAttribute('value') });
                            var theForm = s.up('form');
                            theForm.appendChild(action);
                            theForm.submit();
                        },
                        function() {
                            // on cancel, do nothing
                            return
                        })
            })
        }
    })

    // "select all" handler
    if ($('listViewSelectAll')) {
        $('listViewSelectAll').observe('click', function(event) {
            // set all the item checkboxes to state of the "select all" checkbox
            var checkedState = $('listViewSelectAll').checked
            allItemSelectCheckboxes.each(function(s) {
                s.checked = checkedState
            })
            updateActionButtons()
        })
    }

    // enable/disable action buttons based on initial page state
    updateActionButtons()
});

/**
 * any buttons created with <g:listViewActionButton> will be enabled/disabled
 * according to their minSelected and maxSelected attributes
 */
function updateActionButtons()  {
    numberItemsSelected = $$('input:checked.listViewSelectItem').length
    $$('input.listViewAction').each(function(s) {
        s.disabled = (parseInt(s.readAttribute('minSelected')) > numberItemsSelected) ||
                (parseInt(s.readAttribute('maxSelected')) < numberItemsSelected)
    })
}

/**
 * creates a confirmation dialog based on the message. callback functions are invoked
 * @param confirmMessage the text to prompt (can be html)
 * @param okHandler the function that's called on "ok"
 * @param cancelHandler the function that's called on "cancel"
 */
function confirmAction(confirmMessage, okHandler, cancelHandler) {

    var s = Dialog.confirm(confirmMessage, {
                className: "bluelighting",
                width:300, height: 200,
                okLabel: listViewI18n._confirmOkLabel,
                cancelLabel: listViewI18n._confirmCancelLabel,
                onOk: function(win) {
                    if (okHandler) {
                        okHandler()
                    }
                },
                onCancel: function(win) {
                    if (cancelHandler) {
                        cancelHandler()
                    }
                    win.close()
                }
    })
}


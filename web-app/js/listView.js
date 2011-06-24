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

/**
 * This file provides js functions for "list view" item selection and actions
 */
Event.observe(window, 'load', function() {
    // add observer to checkboxes for enabling / disabling checkboxes
    var allItemSelectCheckboxes = $$('input.listViewSelectItem');
    allItemSelectCheckboxes.each(function(item) {
        Event.observe(item, 'click', updateActionButtons);
    })

    // add observer to action buttons with confirmation messages
    $$('input.listViewAction').each (function(s) {
        var confirmMsg = s.readAttribute('confirmMessage')
        if (confirmMsg) {
            Event.observe(s, 'click', function(e){
               if (!confirm(confirmMsg)) {
                   Event.stop(e)
               }
            })
        }
    })

    // "select all" handler
    $('listViewSelectAll').observe('click', function(event) {
        // set all the item checkboxes to state of the "select all" checkbox
        var checkedState = $('listViewSelectAll').checked
        allItemSelectCheckboxes.each(function(s) {
            s.checked = checkedState
        })
        updateActionButtons()
    })

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


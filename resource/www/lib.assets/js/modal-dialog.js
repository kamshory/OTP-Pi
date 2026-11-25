(function (window, $) {
    'use strict';

    $(function () {
        var queue = [];
        var activeDialog = null;
        var activeResult;
        var hasResult = false;
        var $dialog = $(
            '<div class="modal fade" id="app-dialog" tabindex="-1" role="dialog" aria-labelledby="app-dialog-title" aria-describedby="app-dialog-message" aria-hidden="true">' +
                '<div class="modal-dialog" role="document">' +
                    '<div class="modal-content">' +
                        '<div class="modal-header">' +
                            '<h4 class="modal-title" id="app-dialog-title"></h4>' +
                        '</div>' +
                        '<div class="modal-body">' +
                            '<p class="app-dialog-message" id="app-dialog-message"></p>' +
                            '<input class="form-control app-dialog-input" type="text" aria-label="Value" autocomplete="off">' +
                        '</div>' +
                        '<div class="modal-footer">' +
                            '<button type="button" class="btn btn-default app-dialog-cancel">Cancel</button>' +
                            '<button type="button" class="btn btn-primary app-dialog-confirm">OK</button>' +
                        '</div>' +
                    '</div>' +
                '</div>' +
            '</div>'
        );

        $('body').append($dialog);

        function defaultResult(type) {
            if (type === 'prompt') {
                return null;
            }
            if (type === 'alert') {
                return true;
            }
            return false;
        }

        function showNextDialog() {
            if (activeDialog || queue.length === 0) {
                return;
            }

            activeDialog = queue.shift();
            hasResult = false;
            $dialog.find('#app-dialog-title').text(activeDialog.title);
            $dialog.find('#app-dialog-message').text(activeDialog.message);
            $dialog.find('.app-dialog-input')
                .toggle(activeDialog.type === 'prompt')
                .val(activeDialog.defaultValue == null ? '' : activeDialog.defaultValue);
            $dialog.find('.app-dialog-cancel').toggle(activeDialog.type !== 'alert');
            $dialog.find('.app-dialog-confirm').text(activeDialog.type === 'alert' ? 'OK' : 'Confirm');
            $dialog.modal({
                backdrop: 'static',
                keyboard: true,
                show: true
            });
        }

        function enqueueDialog(dialog) {
            queue.push(dialog);
            showNextDialog();
        }

        function closeDialog(result) {
            if (!activeDialog) {
                return;
            }
            activeResult = result;
            hasResult = true;
            $dialog.modal('hide');
        }

        $dialog.on('shown.bs.modal', function () {
            if (activeDialog && activeDialog.type === 'prompt') {
                $dialog.find('.app-dialog-input').trigger('focus');
            } else {
                $dialog.find('.app-dialog-confirm').trigger('focus');
            }
        });

        $dialog.on('hidden.bs.modal', function () {
            var completedDialog = activeDialog;
            var result = hasResult ? activeResult : defaultResult(completedDialog.type);
            activeDialog = null;
            activeResult = undefined;
            hasResult = false;

            try {
                if (typeof completedDialog.callback === 'function') {
                    completedDialog.callback(result);
                }
            } finally {
                showNextDialog();
            }
        });

        $dialog.on('click', '.app-dialog-cancel', function () {
            closeDialog(defaultResult(activeDialog.type));
        });

        $dialog.on('click', '.app-dialog-confirm', function () {
            if (activeDialog.type === 'prompt') {
                closeDialog($dialog.find('.app-dialog-input').val());
            } else {
                closeDialog(true);
            }
        });

        window.showAppAlert = function (message, callback) {
            enqueueDialog({
                type: 'alert',
                title: 'Notice',
                message: String(message),
                callback: callback
            });
        };

        window.showAppConfirm = function (message, callback) {
            enqueueDialog({
                type: 'confirm',
                title: 'Confirm action',
                message: String(message),
                callback: callback
            });
        };

        window.showAppPrompt = function (message, defaultValue, callback) {
            enqueueDialog({
                type: 'prompt',
                title: 'Enter value',
                message: String(message),
                defaultValue: defaultValue,
                callback: callback
            });
        };

        $(document).on('click', '[data-confirm]', function (event) {
            var $button = $(this);

            if ($button.data('appDialogApproved')) {
                $button.removeData('appDialogApproved');
                return;
            }

            event.preventDefault();
            if ($button.data('appDialogPending')) {
                return;
            }

            $button.data('appDialogPending', true);
            window.showAppConfirm($button.attr('data-confirm'), function (confirmed) {
                $button.removeData('appDialogPending');
                if (!confirmed) {
                    return;
                }

                $button.data('appDialogApproved', true);
                $button.trigger('click');
            });
        });
    });
}(window, jQuery));

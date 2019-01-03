function selectButton(id) {
  $('label[for="'+ id + '"]').addClass('button-pressed');
}

function deselectButtons() {
  $('label.button').removeClass('button-pressed');
}

function toggleButtons(event) {
  let eventTarget = $(event.target);
  let eventTargetId = eventTarget.attr('id');
  let eventTargetValue = eventTarget.attr('value');
  deselectButtons();
  selectButton(eventTargetId);
}

function resetPanes() {
  $('#transcript-pane').empty();
  $('#entity-pane iframe').attr('src', 'about:blank');
}

function displayLoadingAnimation() {
  $('#transcript-pane').html(
    '<div class="spinner"><div class="spinner-container"><span class="spinner-animation"></span></div></div>'
  );
}

function transcribe(event) {
  let eventTarget = $(event.target);
  let eventTargetValue = eventTarget.attr('value');
  toggleButtons(event);

  $.getJSON(
    'assets/data/' + eventTargetValue + '-transcript.json',
    function(data, status, xhr) {
      xhr.onloadstart = function() {
        resetPanes();
        displayLoadingAnimation();
      };
      xhr.onload = function() {
        resetPanes();
      };
      typeTranscript(data);
    }
  );
}

function upload(event) {
  let eventTarget = $(event.target);
  toggleButtons(event);

  let formData = new FormData();
  formData.append('audio-upload', eventTarget[0].files[0]);

  $.ajax({
    url: 'api/transcript',
    type: 'POST',
    data: formData,
    processData: false,
    contentType: false,
    beforeSend: function(xhr, settings) {
      xhr.onloadstart = function() {
        resetPanes();
        displayLoadingAnimation();
      };
      xhr.onload = function() {
        resetPanes();
      };
    },
    success: typeTranscript
  });
}

function typeTranscript(transcripts) {
  let target = $('#transcript-pane');

  function typeWords(words, callback) {
    function typeIntoTarget(i) {
      let item = words[i];
      target.append(item.word + ' ');
      setTimeout(
        function(j) {
          if ((i == words.length - 1) && callback) {
            callback();
          } else if (j < words.length) {
            typeIntoTarget(j);
          }
        },
        item.utteranceMillis,
        i + 1
      );
    }
    if (words.length) {
      typeIntoTarget(0);
    }
  }

  if (transcripts.length > 1) {
    typeWords(
      transcripts[0].words,
      function() { typeTranscript(transcripts.slice(1)); }
    );
  } else if (transcripts.length == 1) {
    typeWords(
      transcripts[0].words,
      target
    );
  }
}

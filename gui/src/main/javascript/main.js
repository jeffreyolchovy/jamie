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
    function(data) {
      resetPanes();
      $.each(data, function(index, item) {
        typeWords(item.words, $('#transcript-pane'));
       });
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
      $.each(data, function(index, item) {
        typeWords(item.words, $('#transcript-pane'));
       });
     }
  });
}

function typeWords(words, target) {
  function typeIntoTarget(index) {
    let item = words[index];
    target.append(item.word + ' ');
    setTimeout(typeIntoTarget, item.utteranceMillis, index + 1);
  }
  typeIntoTarget(0);
}

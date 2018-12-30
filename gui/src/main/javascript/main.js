function selectButton(collection) {
  collection.addClass("button-pressed");
}

function deselectButtons(collection) {
  collection.removeClass("button-pressed");
}

function transcribe(event) {
  let eventTarget = $(event.target);
  let eventTargetId = eventTarget.attr('id');
  deselectButtons($('label.button'));
  selectButton($('label[for="'+ eventTargetId + '"]'));
  $("#transcript-pane").html(mockTranscript);
}

function transcode(event) {
  let eventTarget = $(event.target);
  let eventTargetId = eventTarget.attr('id');
  deselectButtons($('label.button'));
  selectButton($('label[for="'+ eventTargetId + '"]'));
  // todo: upload audio file for transcoding and proceed with normal transcription and entity detection
}

var mockTranscript = '<p>The quick brown <a href="https://en.m.wikipedia.org/wiki/Fox" target="entity-iframe">fox</a> jumped over the lazy dog.</p><p>Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vestibulum orci magna, suscipit quis tempus sit amet, congue vel sem. Sed sit amet leo risus. Curabitur viverra nisi ultrices tellus varius lobortis. Praesent ullamcorper et lacus eget convallis. Mauris quis volutpat felis, non pulvinar quam. Nunc at turpis eleifend, ullamcorper nunc ut, malesuada nunc. Aliquam tincidunt rhoncus condimentum. Fusce porttitor felis turpis, quis luctus est volutpat non. Etiam auctor posuere vulputate. Nam faucibus nec metus quis tristique. Aenean imperdiet mauris vel molestie egestas.</p><p>Cras lobortis, elit a dignissim blandit, neque turpis rutrum est, quis facilisis libero arcu ac odio. Sed sollicitudin erat turpis, vel congue augue tincidunt sed. In risus diam, posuere sed tellus in, convallis aliquet dolor. Morbi a urna urna. Etiam semper lorem nec auctor condimentum. Etiam eget turpis quis massa elementum pretium. Pellentesque eu mi et sapien eleifend tempor. Suspendisse blandit diam felis, vel facilisis neque ullamcorper vel.</p><p>The quick brown <a href="https://en.m.wikipedia.org/wiki/Fox" target="entity-iframe">fox</a> jumped over the lazy dog.</p><p>Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vestibulum orci magna, suscipit quis tempus sit amet, congue vel sem. Sed sit amet leo risus. Curabitur viverra nisi ultrices tellus varius lobortis. Praesent ullamcorper et lacus eget convallis. Mauris quis volutpat felis, non pulvinar quam. Nunc at turpis eleifend, ullamcorper nunc ut, malesuada nunc. Aliquam tincidunt rhoncus condimentum. Fusce porttitor felis turpis, quis luctus est volutpat non. Etiam auctor posuere vulputate. Nam faucibus nec metus quis tristique. Aenean imperdiet mauris vel molestie egestas.</p><p>Cras lobortis, elit a dignissim blandit, neque turpis rutrum est, quis facilisis libero arcu ac odio. Sed sollicitudin erat turpis, vel congue augue tincidunt sed. In risus diam, posuere sed tellus in, convallis aliquet dolor. Morbi a urna urna. Etiam semper lorem nec auctor condimentum. Etiam eget turpis quis massa elementum pretium. Pellentesque eu mi et sapien eleifend tempor. Suspendisse blandit diam felis, vel facilisis neque ullamcorper vel.</p>'

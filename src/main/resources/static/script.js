$(document).ready(function() {
            $("#selectNumber").change(function() {
                const numTextboxes = parseInt($(this).val());
                const container = $("#textboxContainer");
                container.empty(); // Clear existing textboxes

                for (let i = 0; i < numTextboxes; i++) {
                    const textbox1 = $("<input>").attr({
                        type: "number",
                        name: "hoursWorkedEachDay",
                         value: "2"
                    });
                    container.append(textbox1);
                    const textbox2 = $("<input>").attr({
                          type: "text",
                          name: "timeOfDay",
                          value: "N/A"
                    });
                   container.append(textbox2);
                }
            });
        });
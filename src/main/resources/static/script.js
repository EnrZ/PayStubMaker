$(document).ready(function() {
            $("#selectNumber").change(function() {
                const numTextboxes = parseInt($(this).val());
                const container = $("#textboxContainer");
                container.empty(); // Clear existing textboxes

                for (let i = 0; i < numTextboxes; i++) {

                const label = $("<label>").text("Day " + (i + 1));
                            container.append(label);

                    const select1 = $("<select>").attr({
                                            name: "hoursWorkedEachDay"
                                        });

                    select1.append($("<option>").val(0).text("Select hours worked"));
                    select1.append($("<option>").val(1).text("1 hour"));
                    select1.append($("<option>").val(2).text("2 hours"));
                    select1.append($("<option>").val(3).text("3 hours"));
                    select1.append($("<option>").val(4).text("4 hours"));
                    select1.append($("<option>").val(5).text("5 hours"));
                    select1.append($("<option>").val(6).text("6 hours"));
                    select1.append($("<option>").val(7).text("7 hours"));
                    select1.append($("<option>").val(8).text("8 hours"));
                    select1.append($("<option>").val(9).text("9 hours"));
                    select1.append($("<option>").val(10).text("10 hours"));
                    select1.append($("<option>").val(11).text("11 hours"));
                    select1.append($("<option>").val(12).text("12 hours"));
                    select1.append($("<option>").val(13).text("13 hours"));
                    select1.append($("<option>").val(14).text("14 hours"));
                    select1.append($("<option>").val(15).text("15 hours"));
                    select1.append($("<option>").val(16).text("16 hours"));
                    select1.append($("<option>").val(17).text("17 hours"));
                    select1.append($("<option>").val(18).text("18 hours"));
                    select1.append($("<option>").val(19).text("19 hours"));
                    select1.append($("<option>").val(20).text("20 hours"));

                    container.append(select1);

                    const select2 = $("<select>").attr({
                        name: "startTime"
                    });
                    select2.append($("<option>").val(0).text("Select starting time"));
                    select2.append($("<option>").val(7).text("7AM"));
                    select2.append($("<option>").val(8).text("8AM"));
                    select2.append($("<option>").val(9).text("9AM"));
                    select2.append($("<option>").val(10).text("10AM"));
                    select2.append($("<option>").val(11).text("11AM"));
                    select2.append($("<option>").val(12).text("Noon"));
                    select2.append($("<option>").val(13).text("1PM"));
                    select2.append($("<option>").val(14).text("2PM"));
                    select2.append($("<option>").val(15).text("3PM"));
                    select2.append($("<option>").val(16).text("4PM"));
                    select2.append($("<option>").val(17).text("5PM"));
                    select2.append($("<option>").val(18).text("6PM"));
                    select2.append($("<option>").val(19).text("7PM"));




                   container.append(select2);

                   container.append("<br>");
                }
            });
        });
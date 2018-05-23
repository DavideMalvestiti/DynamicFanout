cd 'C:\Users\Mago\workspace\DynamicFanout\peersim\script'
set palette maxcolors 5
set cbrange [0:4]
set palette defined ( 0 "black", 1 "green", 2 "red", 3 "yellow", 4 "blue" )
unset colorbox

set style line 1 lc rgb '#0060ad' lt 1 lw 2.5 pt 1
set style line 2 lc rgb "blue" lt 1 lw 6.5 pt 1
set style line 3 pt 7 ps 2.5 palette

do for [i=0:14] {
plot 'base.dat' notitle with linespoints ls 1 , 'receivers'.i.'.dat' notitle with linespoints ls 2 , 'bleStates'.i.'.dat' using 1:2:3 notitle with linespoints ls 3 , NaN with point pt 5 ps 2 lc rgb "black" title "Standby", NaN with points pt 5 ps 2 lc rgb "green" title "Scanning", NaN with points pt 5 ps 2 lc rgb "red" title "Initiating", NaN with points pt 5 ps 2 lc rgb "yellow" title "Advertising", NaN with points pt 5 ps 2 lc rgb "blue" title "Connection"

pause 2
}

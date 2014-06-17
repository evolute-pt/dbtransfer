/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pt.evolute.dbtransfer.transfer;

import java.util.List;

/**
 *
 * @author lflores
 */
public class ReportThread extends Thread
{
    private static final int SLEEP_TIME_MS = 5000;
    
    private final Mover mover;
    private final List<AsyncStatement> threads;
    
    private boolean running = true;
    
    private long lastReport = System.currentTimeMillis();
    
    public ReportThread( Mover mov, List<AsyncStatement> list )
    {
        mover = mov;
        threads = list;
        setDaemon( true );
    }
    
    @Override
    public void run()
    {
        int lastRead = 0;
        while( running || !threads.isEmpty() )
        {
            System.out.println();
            if( !running )
            {
                System.out.println( "WAITING WRITES" );
            }
            int r = mover.getReadCount();
            long last = System.currentTimeMillis();
            System.out.println( "READ: " + ( r - lastRead ) 
                    + " in " + ( last - lastReport ) + "ms sleeping: " + mover.isSleeping() );
            lastRead = r;
            if( threads.size() > 1 )
            {
                System.out.println( "WRITING THREADS: " + threads.size() );
            }
            for( AsyncStatement as: threads )
            {
                int writeRows = as.getAndResetWriteRows();
                int privateRows = as.getPrivateRowsSize();
                int sharedRows = as.getSharedRowsSize();
                last = System.currentTimeMillis();
                System.out.println( "WRITE: " + as.getName() + " rows: " 
                        + writeRows + " in " + ( last - lastReport ) + "ms " 
                        + ( as.isSleeping()? "sleeping!": "" )
                        + " shared rows: " + sharedRows
                        + " private rows: " + privateRows );
                long totalMem = Runtime.getRuntime().totalMemory();
                long freeMem = Runtime.getRuntime().freeMemory();
                System.out.println( "free/allocated JVM memory: " + freeMem / (1024*1024) 
                        + "/" + totalMem / (1024*1024) + "MB" );
                lastReport = last;
//                System.gc();
            }
            try
            {
                sleep( SLEEP_TIME_MS );
            }
            catch( InterruptedException ex )
            {
            }
        }
    }
    
    public void stopReporting()
    {
        running = false;
    }
}

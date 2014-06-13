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
            System.out.println( "READ: " + ( r - lastRead ) 
                    + " in " + SLEEP_TIME_MS + "ms sleeping: " + mover.isSleeping() );
            lastRead = r;
            if( threads.size() > 1 )
            {
                System.out.println( "WRITING THREADS: " + threads.size() );
            }
            for( AsyncStatement as: threads )
            {
                System.out.println( "WRITE: id: " + as.getName() + " rows: " 
                        + as.getAndResetWriteRows() + " in " + SLEEP_TIME_MS + "ms " 
                        + ( as.isSleeping()? "sleeping!": "" )
                        + " shared rows: " + as.getSharedRowsSize()
                        + " private rows: " + as.getPrivateRowsSize());
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

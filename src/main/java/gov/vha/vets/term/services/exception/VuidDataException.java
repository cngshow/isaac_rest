/**
 * 
 */
package gov.vha.vets.term.services.exception;

/**
 * @author VHAISLMURDOH
 *
 */
public class VuidDataException extends Exception
{
        private static final long serialVersionUID = -5110033457145588385L;
        
        public VuidDataException()
        {
            super();
        }

        /**
         * @param message
         */
        public VuidDataException(String message)
        {
            super(message);
        }

        /**
         * @param cause
         */
        public VuidDataException(Throwable cause)
        {
            super(cause);
        }

        /**
         * @param message
         * @param cause
         */
        public VuidDataException(String message, Throwable cause)
        {
            super(message, cause);
        }
}

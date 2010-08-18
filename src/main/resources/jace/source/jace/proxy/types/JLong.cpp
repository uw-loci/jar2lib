
#include "jace/proxy/types/JLong.h"

#ifndef JACE_JCLASS_IMPL_H
#include "jace/JClassImpl.h"
#endif
using jace::JClassImpl;

#include "jace/BoostWarningOff.h"
#include <boost/thread/mutex.hpp>
#include "jace/BoostWarningOn.h"

BEGIN_NAMESPACE_3( jace, proxy, types )


JLong::JLong( jvalue value )
{
  setJavaJniValue( value );
}

JLong::JLong( jlong long_ )
{
  jvalue value;
  value.j = long_;
  setJavaJniValue( value );
}

JLong::JLong( const JInt& int_ )
{
  jvalue value;
  value.j = int_.getInt();
  setJavaJniValue( value );
}


JLong::~JLong()
{}

JLong::operator jlong() const
{
  return getJavaJniValue().j;
}

jlong JLong::getLong() const
{
  return getJavaJniValue().j;
}

bool JLong::operator==( const JLong& long_ ) const
{
  return long_.getLong() == getLong();
}

bool JLong::operator!=( const JLong& long_ ) const
{
  return !( *this == long_ );
}

bool JLong::operator==( jlong val ) const
{
  return val == getLong();
}

bool JLong::operator!=( jlong val ) const
{
  return ! ( *this == val );
}

static boost::mutex javaClassMutex;
const JClass& JLong::staticGetJavaJniClass() throw ( JNIException )
{
	static boost::shared_ptr<JClassImpl> result;
	boost::mutex::scoped_lock lock(javaClassMutex);
	if (result == 0)
		result = boost::shared_ptr<JClassImpl>(new JClassImpl("long", "J"));
	return *result;
}

const JClass& JLong::getJavaJniClass() const throw ( JNIException )
{
  return JLong::staticGetJavaJniClass();
}

END_NAMESPACE_3( jace, proxy, types )

